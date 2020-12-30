package bot.main;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static String get(String key) {
        String value = dotenv.get(key.toUpperCase());
        System.out.println();
        return value;

    }

}
