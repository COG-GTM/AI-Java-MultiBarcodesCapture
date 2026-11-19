// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class EBarcodesSymbologiesTest {

    @ParameterizedTest
    @EnumSource(EBarcodesSymbologies.class)
    void fromNameRoundTrips(EBarcodesSymbologies symbology) {
        assertEquals(symbology, EBarcodesSymbologies.fromName(symbology.getName()));
    }

    @ParameterizedTest
    @EnumSource(EBarcodesSymbologies.class)
    void fromIntRoundTrips(EBarcodesSymbologies symbology) {
        assertEquals(symbology, EBarcodesSymbologies.fromInt(symbology.getIntValue()));
    }

    @ParameterizedTest
    @EnumSource(EBarcodesSymbologies.class)
    void fromLabelTypeRoundTrips(EBarcodesSymbologies symbology) {
        assertEquals(symbology, EBarcodesSymbologies.fromLabelType(symbology.getLabelType()));
    }

    @ParameterizedTest
    @EnumSource(EBarcodesSymbologies.class)
    void toStringReturnsDisplayName(EBarcodesSymbologies symbology) {
        assertEquals(symbology.getName(), symbology.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "qrcode", "NOT A SYMBOLOGY", "LABEL-TYPE-QRCODE"})
    void unknownNameMapsToUnknown(String name) {
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromName(name));
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, 46, 100, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void unknownIntMapsToUnknown(int value) {
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromInt(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "QRCODE", "LABEL-TYPE-qrcode", "LABEL-TYPE-NOPE"})
    void unknownLabelTypeMapsToUnknown(String labelType) {
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromLabelType(labelType));
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(NullPointerException.class, () -> EBarcodesSymbologies.fromName(null));
        assertThrows(NullPointerException.class, () -> EBarcodesSymbologies.fromLabelType(null));
    }

    @Test
    void matchesWebInterfaceSymbologyTable() throws IOException {
        Path initSql = Path.of("..", "WebInterface", "database", "init.sql");
        String sql = new String(Files.readAllBytes(initSql), StandardCharsets.UTF_8);
        String insert = sql.substring(sql.indexOf("INSERT INTO symbologies"));
        insert = insert.substring(0, insert.indexOf(';'));

        Map<Integer, String> dbSymbologies = new TreeMap<>();
        Matcher row = Pattern.compile("\\((-?\\d+),\\s*'([^']+)'\\)").matcher(insert);
        while (row.find()) {
            dbSymbologies.put(Integer.parseInt(row.group(1)), row.group(2));
        }

        Map<Integer, String> appSymbologies = Arrays.stream(EBarcodesSymbologies.values())
                .collect(Collectors.toMap(EBarcodesSymbologies::getIntValue,
                        EBarcodesSymbologies::getName, (a, b) -> a, TreeMap::new));

        assertEquals(appSymbologies, dbSymbologies);
    }
}
