// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.filemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class EExportModeTest {

    @ParameterizedTest
    @CsvSource({"TEXT, .txt", "CSV, .csv", "EXCEL, .xlsx"})
    void extensionPerMode(EExportMode mode, String extension) {
        assertEquals(extension, mode.getExtension());
    }

    @ParameterizedTest
    @EnumSource(EExportMode.class)
    void fromExtensionRoundTrips(EExportMode mode) {
        assertEquals(mode, EExportMode.fromExtension(mode.getExtension()));
    }

    @ParameterizedTest
    @CsvSource({".pdf, TEXT", "'', TEXT", "csv, TEXT", ".CSV, TEXT"})
    void unknownExtensionFallsBackToText(String extension, EExportMode expected) {
        assertEquals(expected, EExportMode.fromExtension(extension));
    }

    @ParameterizedTest
    @EnumSource(EExportMode.class)
    void toStringIsModeName(EExportMode mode) {
        assertEquals(mode.name(), mode.toString());
    }
}
