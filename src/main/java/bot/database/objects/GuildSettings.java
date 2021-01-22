package bot.database.objects;

import bot.Config;
import org.bson.Document;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuildSettings {

    public final String prefix;
    public final boolean flagTranslation;
    public final List<String> translationChannels;

    public GuildSettings() {
        this.prefix = Config.get("PREFIX");
        this.flagTranslation = false;
        this.translationChannels = new ArrayList<>();
    }

    public GuildSettings(Document document) {
        this.prefix = document.getString("prefix");
        this.flagTranslation = document.containsKey("flag_translation") && document.getBoolean("flag_translation");
        this.translationChannels = document.containsKey("translation_channels")
                ? document.getList("translation_channels", String.class)
                : new ArrayList<>();
    }

    public GuildSettings(ResultSet rs) throws SQLException {
        this.prefix = rs.getString("prefix");
        this.flagTranslation = rs.getInt("flag_translation") == 1;
        this.translationChannels = Arrays.asList(rs.getString("translation_channels").split(","));
    }

}
