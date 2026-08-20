package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class EProcessingModeTest {

    @Test
    public void toStringReturnsPersistedKey() {
        assertEquals("file", EProcessingMode.FILE.toString());
        assertEquals("https_post", EProcessingMode.HTTPSPOST.toString());
    }

    @Test
    public void fromKeyRoundTripsEveryDeclaredKey() {
        for (EProcessingMode mode : EProcessingMode.values()) {
            assertEquals(mode, EProcessingMode.fromKey(mode.toString()));
        }
    }

    @Test
    public void fromKeyFallsBackToFileForUnknownKey() {
        assertEquals(EProcessingMode.FILE, EProcessingMode.fromKey(""));
        assertEquals(EProcessingMode.FILE, EProcessingMode.fromKey("FILE"));
        assertEquals(EProcessingMode.FILE, EProcessingMode.fromKey("http_post"));
    }

    @Test
    public void fromKeyThrowsOnNullKey() {
        assertThrows(NullPointerException.class, () -> EProcessingMode.fromKey(null));
    }
}
