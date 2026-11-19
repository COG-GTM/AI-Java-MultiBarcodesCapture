#!/usr/bin/env python3
"""Contract test: drives every operation in openapi.yaml against a running WebInterface and
validates each request and response against the spec.

Usage: python api-spec/contract_test.py [--base-url http://localhost:3500/api] [--allow-reset]
The run creates and then deletes its own sessions; `--allow-reset` also exercises
DELETE ?reset=all, which wipes ALL data (use only on disposable databases, e.g. CI).
"""
import argparse
import json
import sys
from pathlib import Path

import requests
from openapi_core import OpenAPI
from openapi_core.contrib.requests import RequestsOpenAPIRequest, RequestsOpenAPIResponse

SPEC = OpenAPI.from_file_path(str(Path(__file__).with_name("openapi.yaml")))


def file_media_types(method, path, status):
    response = SPEC.spec / "paths" / path / method.lower() / "responses" / str(status)
    return set((response / "content").keys()) if "content" in response else set()


class Client:
    def __init__(self, base_url):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.checks = 0

    def call(self, method, path, expected_status, params=None, body=None):
        req = requests.Request(method, self.base_url + path, params=params,
                               data=None if body is None else json.dumps(body),
                               headers={"Content-Type": "application/json"} if body is not None else {})
        prepared = self.session.prepare_request(req)
        resp = self.session.send(prepared, timeout=30)
        label = f"{method} {path} {params or ''}".strip()
        assert resp.status_code == expected_status, f"{label}: expected {expected_status}, got {resp.status_code}: {resp.text[:300]}"
        openapi_req = RequestsOpenAPIRequest(prepared)
        # Negative cases send intentionally invalid requests; only their error responses are validated.
        if expected_status < 400:
            SPEC.validate_request(openapi_req)
        mimetype = resp.headers.get("Content-Type", "").split(";")[0].strip()
        if path == "/export.php" and resp.status_code >= 400 and mimetype == "text/html":
            # Known mismatch: export.php error bodies are JSON but sent without a JSON Content-Type.
            assert isinstance(resp.json().get("error"), str), f"{label}: expected JSON error body"
        elif mimetype == "application/json" or not resp.content:
            SPEC.validate_response(openapi_req, RequestsOpenAPIResponse(resp))
        elif not file_media_types(method, path, resp.status_code):
            SPEC.validate_response(openapi_req, RequestsOpenAPIResponse(resp))
        else:
            # openapi-core cannot unmarshal file downloads; check the declared media type and attachment header.
            declared = file_media_types(method, path, resp.status_code)
            assert mimetype in declared, f"{label}: {mimetype} not declared in {declared}"
            assert resp.headers.get("Content-Disposition", "").startswith("attachment;"), f"{label}: missing attachment header"
        self.checks += 1
        print(f"ok  {resp.status_code} {label}")
        return resp


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:3500/api")
    parser.add_argument("--allow-reset", action="store_true")
    args = parser.parse_args()
    if args.base_url.rstrip("/") not in [s["url"] for s in SPEC.spec / "servers"]:
        sys.exit(f"--base-url must be one of the servers declared in openapi.yaml")
    c = Client(args.base_url)

    capture = {
        "session_timestamp": "2026-01-15T10:00:00",
        "device_info": "contract-test",
        "device_ip": "10.0.0.5",
        "barcodes": [
            {"value": "0123456789012", "symbology": 7, "quantity": 2, "timestamp": "2026-01-15T10:00:00"},
            {"value": "QR-1", "symbology": 2, "quantity": 1, "timestamp": "2026-01-15T10:00:01"},
        ],
    }
    s1 = int(c.call("POST", "/barcodes.php", 200, body=capture).json()["session_id"])
    s2 = int(c.call("POST", "/barcodes.php", 200, body=capture).json()["session_id"])
    c.call("POST", "/barcodes.php", 400, body={"device_info": "missing barcodes"})

    sessions = c.call("GET", "/barcodes.php", 200).json()["sessions"]
    assert {s1, s2} <= {s["id"] for s in sessions}
    details = c.call("GET", "/barcodes.php", 200, params={"session_id": s1}).json()
    assert [b["value"] for b in details["barcodes"]] == ["0123456789012", "QR-1"]
    c.call("GET", "/barcodes.php", 404, params={"session_id": 999999999})

    barcode_id = details["barcodes"][0]["id"]
    c.call("PUT", "/barcodes.php", 200, params={"barcode_id": barcode_id}, body={"processed": True, "notes": "checked"})
    updated = c.call("PUT", "/barcodes.php", 200, body={
        "action": "update_barcode", "barcode_id": barcode_id, "value": "9999", "symbology": 2, "quantity": 3}).json()
    assert updated["updated_fields"]["symbology_name"] == "UPC A"
    c.call("PUT", "/barcodes.php", 400, body={
        "action": "update_barcode", "barcode_id": barcode_id, "value": "9999", "symbology": 2, "quantity": 0})

    c.call("GET", "/server-info.php", 200)
    for fmt in ["txt", "csv", "xlsx", "excel"]:
        c.call("POST", "/export.php", 200, body={"session_ids": [s1], "format": fmt})
    c.call("POST", "/export.php", 400, body={"session_ids": [s1], "format": "pdf"})

    merged = c.call("POST", "/barcodes.php", 200, body={"action": "bulk_merge", "session_ids": [s1, s2]}).json()
    new_id = int(merged["new_session_id"])
    c.call("DELETE", "/barcodes.php", 200, body={"action": "bulk_delete", "session_ids": [s1, s2, new_id]})
    c.call("OPTIONS", "/barcodes.php", 200)

    if args.allow_reset:
        c.call("DELETE", "/barcodes.php", 200, params={"reset": "all"})

    print(f"\n{c.checks} operations validated against openapi.yaml")


if __name__ == "__main__":
    main()
