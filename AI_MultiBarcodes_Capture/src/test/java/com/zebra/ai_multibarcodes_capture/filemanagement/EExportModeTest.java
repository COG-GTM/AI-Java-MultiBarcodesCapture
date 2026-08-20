package com.zebra.ai_multibarcodes_capture.filemanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class EExportModeTest {

    @Test
    public void toStringReturnsDisplayName() {
        assertEquals("TEXT", EExportMode.TEXT.toString());
        assertEquals("CSV", EExportMode.CSV.toString());
        assertEquals("EXCEL", EExportMode.EXCEL.toString());
    }

    @Test
    public void getExtensionReturnsExpectedExtension() {
        assertEquals(".txt", EExportMode.TEXT.getExtension());
        assertEquals(".csv", EExportMode.CSV.getExtension());
        assertEquals(".xlsx", EExportMode.EXCEL.getExtension());
    }

    @Test
    public void fromExtensionResolvesEveryDeclaredExtension() {
        for (EExportMode mode : EExportMode.values()) {
            assertEquals(mode, EExportMode.fromExtension(mode.getExtension()));
        }
    }

    @Test
    public void fromExtensionFallsBackToTextForUnsupportedExtension() {
        assertEquals(EExportMode.TEXT, EExportMode.fromExtension(""));
        assertEquals(EExportMode.TEXT, EExportMode.fromExtension(".xls"));
        assertEquals(EExportMode.TEXT, EExportMode.fromExtension("csv"));
        assertEquals(EExportMode.TEXT, EExportMode.fromExtension(".CSV"));
    }

    @Test
    public void fromExtensionThrowsOnNullInput() {
        assertThrows(NullPointerException.class, () -> EExportMode.fromExtension(null));
    }
}
