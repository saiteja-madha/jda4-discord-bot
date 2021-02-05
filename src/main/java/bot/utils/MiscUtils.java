package bot.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MiscUtils {

    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

    public static boolean isURL(String url) {
        return url.matches("^https?:\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_|]");
    }

    public static boolean isImageUrl(String url) {
        Image image;
        try {
            image = ImageIO.read(new URL(url));
            if (image != null)
                return true;
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static boolean isHex(String input) {
        Pattern pattern = Pattern.compile(HEX_PATTERN);
        return pattern.matcher(input).matches();
    }

    public static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
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

    public static String escapeString(@NotNull String string) {
        return string.replace("\\", "\\\\").replace("<", "\\<").replace(">", "\\>").replace("`", "\\`")
                .replace("*", "\\*").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]")
                .replace("(", "\\(").replace(")", "\\)").replace("#", "\\#").replace("+", "\\+").replace("-", "\\-")
                .replace(".", "\\.").replace("!", "\\!").replace("_", "\\_").replace("\"", "\\\"").replace("$", "\\$")
                .replace("%", "\\%").replace("&", "\\&").replace("'", "\\'").replace(",", "\\,").replace("/", "\\/")
                .replace(":", "\\:").replace(";", "\\;").replace("=", "\\=").replace("?", "\\?").replace("@", "\\@")
                .replace("^", "\\^").replace("\n", "\\n").replace("\r", "\\r").replace("|", "\\|");
    }

    public static String escapeMentions(@NotNull String string) {
        return string.replace("@everyone", "@\u0435veryone")
                .replace("@here", "@h\u0435re")
                .replace("discord.gg/", "dis\u0441ord.gg/");
    }

    public static int parseTime(String timeString) {
        timeString = timeString.replaceAll("(?i)(\\s|,|and)", "")
                .replaceAll("(?is)(-?\\d+|[a-z]+)", "$1 ")
                .trim();
        String[] vals = timeString.split("\\s+");
        int timeinseconds = 0;
        try {
            for (int j = 0; j < vals.length; j += 2) {
                int num = Integer.parseInt(vals[j]);
                if (vals[j + 1].toLowerCase().startsWith("m"))
                    num *= 60;
                else if (vals[j + 1].toLowerCase().startsWith("h"))
                    num *= 60 * 60;
                else if (vals[j + 1].toLowerCase().startsWith("d"))
                    num *= 60 * 60 * 24;
                timeinseconds += num;
            }
        } catch (Exception ex) {
            return 0;
        }
        return timeinseconds;
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
