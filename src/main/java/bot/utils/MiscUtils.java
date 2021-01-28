package bot.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Random;
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

    public static String getRemainingTime(Instant instant) {
        Instant plus = instant.plus(1, ChronoUnit.DAYS);
        Duration duration = Duration.between(Instant.now(), plus);

        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%s hours, %s minutes, %s seconds", hours, minutes, seconds);

    }

    public static int getRandInt(int from, int to) {
        final Random rand = new Random();
        if (from > to) {
            from ^= to;
            to ^= from;
            from ^= to;
        }
        return rand.nextInt(to - from) + from;
    }

}
