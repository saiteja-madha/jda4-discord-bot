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
        this.prefix = doc.get("prefix", Config.get("PREFIX"));
        this.flagTranslation = doc.get("flag_translation", false);
        this.translationChannels = doc.get("translation_channels", new ArrayList<>());
        this.isRankingEnabled = doc.get("ranking_enabled", false);
        this.levelUpMessage = doc.get("levelup_message", Config.get("DEFAULT_LEVELUP_MESSAGE"));
        this.levelUpChannel = doc.getString("levelup_channel");
        this.maxWarnings = doc.get("max_warnings", 3);
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
