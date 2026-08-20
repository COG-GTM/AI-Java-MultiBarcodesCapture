package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.models.LanguageItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class LocaleHelperTest {

    private Context context;
    private Locale initialDefaultLocale;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        initialDefaultLocale = Locale.getDefault();
    }

    @After
    public void tearDown() {
        Locale.setDefault(initialDefaultLocale);
    }

    @Test
    public void getLanguageListPairsEveryNameWithItsCode() {
        String[] names = context.getResources().getStringArray(R.array.language_names);
        String[] codes = context.getResources().getStringArray(R.array.language_codes);

        List<LanguageItem> languages = LocaleHelper.getLanguageList(context);

        assertEquals(Math.min(names.length, codes.length), languages.size());
        for (int i = 0; i < languages.size(); i++) {
            assertEquals(names[i], languages.get(i).getLanguageName());
            assertEquals(codes[i], languages.get(i).getLanguageCode());
        }
    }

    @Test
    public void getLanguageListExposesUniqueCodesAndKnownFlags() {
        List<LanguageItem> languages = LocaleHelper.getLanguageList(context);

        Set<String> codes = new HashSet<>();
        List<String> withoutKnownFlag = new ArrayList<>();
        for (LanguageItem language : languages) {
            assertTrue("duplicate code " + language.getLanguageCode(), codes.add(language.getLanguageCode()));
            assertFalse(language.getFlagEmoji().isEmpty());
            if ("🏴".equals(language.getFlagEmoji())) {
                withoutKnownFlag.add(language.getLanguageCode());
            }
        }
        assertTrue("language codes without a dedicated flag: " + withoutKnownFlag, withoutKnownFlag.isEmpty());
        assertTrue(codes.contains("system"));
    }

    @Test
    public void getLanguageListUsesTheGlobeForTheSystemEntry() {
        for (LanguageItem language : LocaleHelper.getLanguageList(context)) {
            if ("system".equals(language.getLanguageCode())) {
                assertEquals("🌐", language.getFlagEmoji());
                return;
            }
        }
        throw new AssertionError("no system language entry");
    }

    @Test
    public void setLocaleKeepsTheSameContextForTheSystemLanguage() {
        assertSame(context, LocaleHelper.setLocale(context, "system"));
        assertEquals(initialDefaultLocale, Locale.getDefault());
    }

    @Test
    public void setLocaleAppliesSimpleLanguageCode() {
        Context localized = LocaleHelper.setLocale(context, "fr");

        assertEquals("fr", Locale.getDefault().getLanguage());
        assertEquals("fr", localized.getResources().getConfiguration().getLocales().get(0).getLanguage());
    }

    @Test
    public void setLocaleAppliesRegionalVariant() {
        Context localized = LocaleHelper.setLocale(context, "zh-rTW");

        Locale applied = localized.getResources().getConfiguration().getLocales().get(0);
        assertEquals("zh", applied.getLanguage());
        assertEquals("TW", applied.getCountry());
    }

    @Test
    public void currentLanguageCodeRoundTripsThroughPreferences() {
        assertEquals(Constants.SHARED_PREFERENCES_LANGUAGE_DEFAULT, LocaleHelper.getCurrentLanguageCode(context));

        LocaleHelper.saveLanguageChoice(context, "de");

        assertEquals("de", LocaleHelper.getCurrentLanguageCode(context));
    }
}
