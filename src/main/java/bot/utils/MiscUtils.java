package bot.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MiscUtils {

    public static boolean isURL(String url) {
        return url.matches("^https?:\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_|]");
    }

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

    public static String escapeMentions(@NotNull String string) {
        return string.replace("@everyone", "@\u0435veryone")
                .replace("@here", "@h\u0435re")
                .replace("discord.gg/", "dis\u0441ord.gg/");
    }

    public static String formatTime(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24L);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        String dayStr = day > 1 ? " Days " : " Day ";
        String hourStr = hours > 1 ? " Hours " : " Hour ";
        String minStr = minute > 1 ? " Minutes " : " Minute ";
        String secStr = minute > 1 ? " Seconds " : " Second ";

        return day + dayStr + hours + hourStr + minute + minStr + second + secStr;
    }

}
