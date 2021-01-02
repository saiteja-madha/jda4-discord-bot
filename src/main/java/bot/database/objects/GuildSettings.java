package bot.database.objects;

import bot.main.Config;

public class GuildSettings {

    public final String prefix;

    public GuildSettings() {
        this.prefix = Config.get("PREFIX");
    }

    public GuildSettings(String prefix) {
        this.prefix = prefix;
    }

}
