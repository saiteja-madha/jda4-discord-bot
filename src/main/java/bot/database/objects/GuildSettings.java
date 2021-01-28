package bot.database.objects;

import bot.Config;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuildSettings {

    public final String prefix;
    public final boolean flagTranslation;
    public final List<String> translationChannels;
    public final boolean isRankingEnabled;
    public final String levelUpMessage;
    @Nullable
    public final String levelUpChannel;

    public GuildSettings() {
        this.prefix = Config.get("PREFIX");
        this.flagTranslation = false;
        this.translationChannels = new ArrayList<>();
        this.isRankingEnabled = true;
        this.levelUpMessage = Config.get("DEFAULT_LEVELUP_MESSAGE");
        this.levelUpChannel = null;
    }

    public GuildSettings(Document document) {
        this.prefix = document.getString("prefix");
        this.flagTranslation = document.containsKey("flag_translation") && document.getBoolean("flag_translation");
        this.translationChannels = document.containsKey("translation_channels")
                ? document.getList("translation_channels", String.class)
                : new ArrayList<>();
        this.isRankingEnabled = document.containsKey("ranking_enabled") && document.getBoolean("ranking_enabled");
        this.levelUpMessage = document.containsKey("levelup_message") ? document.getString("levelup_message") :
                Config.get("DEFAULT_LEVELUP_MESSAGE");
        this.levelUpChannel = document.containsKey("levelup_channel") ? document.getString("levelup_channel") : null;
    }

    public GuildSettings(ResultSet rs) throws SQLException {
        this.prefix = rs.getString("prefix");
        this.flagTranslation = rs.getInt("flag_translation") == 1;
        this.translationChannels = Arrays.asList(rs.getString("translation_channels").split(","));
        this.isRankingEnabled = rs.getInt("ranking_enabled") == 1;
        this.levelUpMessage = rs.getString("levelup_message");
        this.levelUpChannel = rs.getString("levelup_channel");
    }

}
