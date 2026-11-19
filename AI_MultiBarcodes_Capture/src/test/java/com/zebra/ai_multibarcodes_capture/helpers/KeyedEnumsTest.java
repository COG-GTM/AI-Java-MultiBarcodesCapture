// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zebra.ai.vision.detector.InferencerOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KeyedEnumsTest {

    @ParameterizedTest
    @CsvSource({"file, FILE", "https_post, HTTPSPOST", "unknown, FILE", "'', FILE"})
    void processingModeFromKey(String key, EProcessingMode expected) {
        assertEquals(expected, EProcessingMode.fromKey(key));
    }

    @ParameterizedTest
    @CsvSource({"press, CAPTURE_ON_PRESS", "release, CAPTURE_ON_RELEASE", "unknown, CAPTURE_ON_PRESS"})
    void captureTriggerModeFromKey(String key, ECaptureTriggerMode expected) {
        assertEquals(expected, ECaptureTriggerMode.fromKey(key));
    }

    @ParameterizedTest
    @CsvSource({"modern, MODERN", "legacy, LEGACY", "unknown, MODERN"})
    void themeFromKey(String key, ETheme expected) {
        assertEquals(expected, ETheme.fromKey(key));
    }

    @Test
    void keysAreExposedThroughToString() {
        assertEquals("file", EProcessingMode.FILE.toString());
        assertEquals("https_post", EProcessingMode.HTTPSPOST.toString());
        assertEquals("press", ECaptureTriggerMode.CAPTURE_ON_PRESS.toString());
        assertEquals("release", ECaptureTriggerMode.CAPTURE_ON_RELEASE.toString());
        assertEquals("modern", ETheme.MODERN.toString());
    }

    @Test
    void inferenceTypeMapsToSdkOptions() {
        assertEquals(InferencerOptions.DSP, EInferenceType.DSP.toInferencerOptions());
        assertEquals(InferencerOptions.GPU, EInferenceType.GPU.toInferencerOptions());
        assertEquals(InferencerOptions.CPU, EInferenceType.CPU.toInferencerOptions());
    }
}
