package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class ECaptureTriggerModeTest {

    @Test
    public void toStringReturnsPersistedKey() {
        assertEquals("press", ECaptureTriggerMode.CAPTURE_ON_PRESS.toString());
        assertEquals("release", ECaptureTriggerMode.CAPTURE_ON_RELEASE.toString());
    }

    @Test
    public void fromKeyRoundTripsEveryDeclaredKey() {
        for (ECaptureTriggerMode mode : ECaptureTriggerMode.values()) {
            assertEquals(mode, ECaptureTriggerMode.fromKey(mode.toString()));
        }
    }

    @Test
    public void fromKeyFallsBackToPressForUnknownKey() {
        assertEquals(ECaptureTriggerMode.CAPTURE_ON_PRESS, ECaptureTriggerMode.fromKey(""));
        assertEquals(ECaptureTriggerMode.CAPTURE_ON_PRESS, ECaptureTriggerMode.fromKey("RELEASE"));
        assertEquals(ECaptureTriggerMode.CAPTURE_ON_PRESS, ECaptureTriggerMode.fromKey("long_press"));
    }

    @Test
    public void fromKeyThrowsOnNullKey() {
        assertThrows(NullPointerException.class, () -> ECaptureTriggerMode.fromKey(null));
    }
}
