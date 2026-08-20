package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class PreferencesHelperTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
    }

    @Test
    public void lastSessionFileIsNullUntilSaved() {
        assertNull(PreferencesHelper.getLastSelectedSession(context));
    }

    @Test
    public void lastSessionFileIsPersistedAndReadBack() {
        PreferencesHelper.saveLastSessionFile(context, "/storage/session.xlsx");

        assertEquals("/storage/session.xlsx", PreferencesHelper.getLastSelectedSession(context));
    }

    @Test
    public void lastSessionFileIsNotOverwrittenByNullOrEmptyPath() {
        PreferencesHelper.saveLastSessionFile(context, "/storage/session.txt");

        PreferencesHelper.saveLastSessionFile(context, null);
        PreferencesHelper.saveLastSessionFile(context, "");

        assertEquals("/storage/session.txt", PreferencesHelper.getLastSelectedSession(context));
    }

    @Test
    public void currentExtensionDefaultsToTheTextExtension() {
        assertEquals(EExportMode.TEXT.getExtension(), PreferencesHelper.getCurrentExtension(context));
        assertEquals(Constants.FILE_DEFAULT_EXTENSION, PreferencesHelper.getCurrentExtension(context));
    }

    @Test
    public void currentExtensionIsPersistedAndReadBack() {
        PreferencesHelper.saveCurrentExtension(context, EExportMode.EXCEL.getExtension());

        assertEquals(".xlsx", PreferencesHelper.getCurrentExtension(context));
        assertEquals(EExportMode.EXCEL, EExportMode.fromExtension(PreferencesHelper.getCurrentExtension(context)));
    }

    @Test
    public void captureZoneDefaultsMatchConstants() {
        assertEquals(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_ENABLED_DEFAULT,
                PreferencesHelper.isCaptureZoneEnabled(context));
        assertEquals(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_X_DEFAULT, PreferencesHelper.getCaptureZoneX(context));
        assertEquals(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_Y_DEFAULT, PreferencesHelper.getCaptureZoneY(context));
        assertEquals(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_WIDTH_DEFAULT,
                PreferencesHelper.getCaptureZoneWidth(context));
        assertEquals(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_HEIGHT_DEFAULT,
                PreferencesHelper.getCaptureZoneHeight(context));
    }

    @Test
    public void captureZonePositionIsPersistedAsAWhole() {
        PreferencesHelper.saveCaptureZonePosition(context, 12, 34, 56, 78);

        assertEquals(12, PreferencesHelper.getCaptureZoneX(context));
        assertEquals(34, PreferencesHelper.getCaptureZoneY(context));
        assertEquals(56, PreferencesHelper.getCaptureZoneWidth(context));
        assertEquals(78, PreferencesHelper.getCaptureZoneHeight(context));
    }

    @Test
    public void captureZoneEnabledIsPersisted() {
        PreferencesHelper.saveCaptureZoneEnabled(context, true);
        assertTrue(PreferencesHelper.isCaptureZoneEnabled(context));

        PreferencesHelper.saveCaptureZoneEnabled(context, false);
        assertFalse(PreferencesHelper.isCaptureZoneEnabled(context));
    }

    @Test
    public void flashlightStateIsPersistedAndDefaultsToConstant() {
        assertEquals(Constants.SHARED_PREFERENCES_FLASHLIGHT_ENABLED_DEFAULT,
                PreferencesHelper.isFlashlightEnabled(context));

        PreferencesHelper.saveFlashlightEnabled(context, true);
        assertTrue(PreferencesHelper.isFlashlightEnabled(context));

        PreferencesHelper.saveFlashlightEnabled(context, false);
        assertFalse(PreferencesHelper.isFlashlightEnabled(context));
    }

    @Test
    public void selectedLanguageDefaultsToConstantAndIsPersisted() {
        assertEquals(Constants.SHARED_PREFERENCES_LANGUAGE_DEFAULT, PreferencesHelper.getSelectedLanguage(context));

        PreferencesHelper.saveSelectedLanguage(context, "zh-rTW");

        assertEquals("zh-rTW", PreferencesHelper.getSelectedLanguage(context));
    }

    @Test
    public void preferencesAreStoredUnderThePackageNameFile() {
        PreferencesHelper.saveCurrentExtension(context, ".csv");

        assertEquals(".csv", context
                .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                .getString(Constants.SHARED_PREFERENCES_EXTENSION, null));
    }
}
