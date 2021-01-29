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
    public final int maxWarnings;

    public GuildSettings() {
        this.prefix = Config.get("PREFIX");
        this.flagTranslation = false;
        this.translationChannels = new ArrayList<>();
        this.isRankingEnabled = true;
        this.levelUpMessage = Config.get("DEFAULT_LEVELUP_MESSAGE");
        this.levelUpChannel = null;
        this.maxWarnings = 3;
    }

    public GuildSettings(Document doc) {
        this.prefix = doc.getString("prefix");
        this.flagTranslation = doc.containsKey("flag_translation") && doc.getBoolean("flag_translation");
        this.translationChannels = doc.containsKey("translation_channels")
                ? doc.getList("translation_channels", String.class)
                : new ArrayList<>();
        this.isRankingEnabled = doc.containsKey("ranking_enabled") && doc.getBoolean("ranking_enabled");
        this.levelUpMessage = doc.containsKey("levelup_message") ? doc.getString("levelup_message") : Config.get("DEFAULT_LEVELUP_MESSAGE");
        this.levelUpChannel = doc.containsKey("levelup_channel") ? doc.getString("levelup_channel") : null;
        this.maxWarnings = doc.containsKey("max_warnings") ? doc.getInteger("max_warnings") : 3;
    }

    public GuildSettings(ResultSet rs) throws SQLException {
        this.prefix = rs.getString("prefix");
        this.flagTranslation = rs.getInt("flag_translation") == 1;
        this.translationChannels = Arrays.asList(rs.getString("translation_channels").split(","));
        this.isRankingEnabled = rs.getInt("ranking_enabled") == 1;
        this.levelUpMessage = rs.getString("levelup_message");
        this.levelUpChannel = rs.getString("levelup_channel");
        this.maxWarnings = rs.getInt("max_warnings");
    }

}
