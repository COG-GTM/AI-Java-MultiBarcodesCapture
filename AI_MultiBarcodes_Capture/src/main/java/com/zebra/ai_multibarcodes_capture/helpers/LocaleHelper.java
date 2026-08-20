package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.models.LanguageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String languageCode) {
        LogUtils.d("LocaleHelper", "Setting locale to: " + languageCode);
        
        if ("system".equals(languageCode)) {
            // Use system default locale
            return context;
        }

        Locale locale;
        if (languageCode.contains("-r")) {
            // Handle regional variants like zh-rTW
            String[] parts = languageCode.split("-r");
            locale = new Locale(parts[0], parts[1]);
        } else {
            locale = new Locale(languageCode);
        }

        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }

    public static List<LanguageItem> getLanguageList(Context context) {
        List<LanguageItem> languages = new ArrayList<>();
        
        String[] languageNames = context.getResources().getStringArray(R.array.language_names);
        String[] languageCodes = context.getResources().getStringArray(R.array.language_codes);
        
        for (int i = 0; i < languageNames.length && i < languageCodes.length; i++) {
            String flagEmoji = getFlagEmoji(languageCodes[i]);
            languages.add(new LanguageItem(languageNames[i], languageCodes[i], flagEmoji));
        }
        
        return languages;
    }

        private static String getFlagEmoji(String languageCode) {
        switch (languageCode) {
            case "system":
                return "🌐"; // Globe icon for system language
            case "ar":
                return "🇸🇦"; // Saudi Arabia for Arabic
            case "af":
                return "🇿🇦"; // South Africa for Afrikaans
            case "sq":
                return "🇦🇱"; // Albania
            case "am":
                return "🇪🇹"; // Ethiopia for Amharic
            case "hy":
                return "🇦🇲"; // Armenia
            case "az":
                return "🇦🇿"; // Azerbaijan
            case "be":
                return "🇧🇾"; // Belarus
            case "bs":
                return "🇧🇦"; // Bosnia and Herzegovina
            case "bn":
                return "🇧🇩"; // Bangladesh for Bengali
            case "bg":
                return "🇧🇬"; // Bulgaria
            case "my":
                return "🇲🇲"; // Myanmar for Burmese
            case "ca":
                return "🇪🇸"; // Spain for Catalan
            case "zh":
                return "🇨🇳"; // China for Simplified Chinese
            case "zh-rTW":
                return "🇹🇼"; // Taiwan for Traditional Chinese
            case "hr":
                return "🇭🇷"; // Croatia
            case "cs":
                return "🇨🇿"; // Czech Republic
            case "da":
                return "🇩🇰"; // Denmark
            case "nl":
                return "🇳🇱"; // Netherlands
            case "en":
                return "🇺🇸"; // United States for English
            case "et":
                return "🇪🇪"; // Estonia
            case "fil":
                return "🇵🇭"; // Philippines for Filipino
            case "fi":
                return "🇫🇮"; // Finland
            case "fr":
                return "🇫🇷"; // France
            case "gl":
                return "🇪🇸"; // Spain for Galician
            case "ka":
                return "🇬🇪"; // Georgia
            case "de":
                return "🇩🇪"; // Germany
            case "el":
                return "🇬🇷"; // Greece
            case "gu":
                return "🇮🇳"; // India for Gujarati
            case "he":
                return "🇮🇱"; // Israel for Hebrew
            case "hi":
                return "🇮🇳"; // India for Hindi
            case "hu":
                return "🇭🇺"; // Hungary
            case "is":
                return "🇮🇸"; // Iceland
            case "id":
                return "🇮🇩"; // Indonesia
            case "it":
                return "🇮🇹"; // Italy
            case "ja":
                return "🇯🇵"; // Japan
            case "jv":
                return "🇮🇩"; // Indonesia for Javanese
            case "kn":
                return "🇮🇳"; // India for Kannada
            case "km":
                return "🇰🇭"; // Cambodia for Khmer
            case "ko":
                return "🇰🇷"; // South Korea
            case "ky":
                return "🇰🇬"; // Kyrgyzstan
            case "lo":
                return "🇱🇦"; // Laos
            case "la":
                return "🇻🇦"; // Vatican for Latin
            case "lv":
                return "🇱🇻"; // Latvia
            case "lt":
                return "🇱🇹"; // Lithuania
            case "mk":
                return "🇲🇰"; // North Macedonia
            case "ms":
                return "🇲🇾"; // Malaysia for Malay
            case "ml":
                return "🇮🇳"; // India for Malayalam
            case "mr":
                return "🇮🇳"; // India for Marathi
            case "mn":
                return "🇲🇳"; // Mongolia
            case "ne":
                return "🇳🇵"; // Nepal
            case "nb":
                return "🇳🇴"; // Norway
            case "fa":
                return "🇮🇷"; // Iran for Persian
            case "pl":
                return "🇵🇱"; // Poland
            case "pt":
                return "🇵🇹"; // Portugal
            case "pa":
                return "🇮🇳"; // India for Punjabi
            case "ro":
                return "🇷🇴"; // Romania
            case "rm":
                return "🇨🇭"; // Switzerland for Romansh
            case "ru":
                return "🇷🇺"; // Russia
            case "sr":
                return "🇷🇸"; // Serbia
            case "si":
                return "🇱🇰"; // Sri Lanka for Sinhala
            case "sk":
                return "🇸🇰"; // Slovakia
            case "sl":
                return "🇸🇮"; // Slovenia
            case "es":
                return "🇪🇸"; // Spain
            case "sw":
                return "🇰🇪"; // Kenya for Swahili
            case "sv":
                return "🇸🇪"; // Sweden
            case "ta":
                return "🇮🇳"; // India for Tamil
            case "te":
                return "🇮🇳"; // India for Telugu
            case "th":
                return "🇹🇭"; // Thailand
            case "tr":
                return "🇹🇷"; // Turkey
            case "uk":
                return "🇺🇦"; // Ukraine
            case "ur":
                return "🇵🇰"; // Pakistan for Urdu
            case "vi":
                return "🇻🇳"; // Vietnam
            case "zu":
                return "🇿🇦"; // South Africa for Zulu
            default:
                return "🏴"; // Generic flag for any missing
        }
    }

    public static String getCurrentLanguageCode(Context context) {
        return PreferencesHelper.getSelectedLanguage(context);
    }

    public static void saveLanguageChoice(Context context, String languageCode) {
        PreferencesHelper.saveSelectedLanguage(context, languageCode);
    }
}