package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class EThemeTest {

    @Test
    public void toStringReturnsPersistedKey() {
        assertEquals("modern", ETheme.MODERN.toString());
        assertEquals("legacy", ETheme.LEGACY.toString());
    }

    @Test
    public void keysMatchTheValuesPersistedInSharedPreferences() {
        assertEquals(Constants.SHARED_PREFERENCES_THEME_DEFAULT, ETheme.MODERN.toString());
        assertEquals(ETheme.MODERN, ETheme.fromKey(Constants.SHARED_PREFERENCES_THEME_DEFAULT));
    }

    @Test
    public void fromKeyRoundTripsEveryDeclaredKey() {
        for (ETheme theme : ETheme.values()) {
            assertEquals(theme, ETheme.fromKey(theme.toString()));
        }
    }

    @Test
    public void fromKeyFallsBackToModernForUnknownKey() {
        assertEquals(ETheme.MODERN, ETheme.fromKey(""));
        assertEquals(ETheme.MODERN, ETheme.fromKey("LEGACY"));
        assertEquals(ETheme.MODERN, ETheme.fromKey("dark"));
    }

    @Test
    public void fromKeyThrowsOnNullKey() {
        assertThrows(NullPointerException.class, () -> ETheme.fromKey(null));
    }
}
