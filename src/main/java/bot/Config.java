package bot;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static String get(String key) {
        String value = dotenv.get(key.toUpperCase());
        if (value == null)
            throw new NullPointerException("Null value for key: " + key);
        return value;
    }

    public static Integer getInt(String key) {
        String value = dotenv.get(key.toUpperCase());
        if (value == null)
            throw new NullPointerException("Null value for key: " + key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Provide an integer value for: " + key);
        }
    }

}
