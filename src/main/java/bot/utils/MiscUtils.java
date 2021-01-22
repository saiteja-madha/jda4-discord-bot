package bot.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MiscUtils {

    @Nullable
    public static String getLanguage(String code) {
        Locale[] availableLocale = Locale.getAvailableLocales();
        for (Locale l : availableLocale) {
            if (l.getLanguage().equalsIgnoreCase(code)) {
                return l.getDisplayLanguage();
            }
        }
        return null;
    }
}
