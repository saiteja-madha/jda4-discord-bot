package bot.database.objects;

import bot.Config;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuildSettings {

    public final String prefix;
    public final List<String> translationChannels;
    public final boolean isRankingEnabled;
    public final String levelUpMessage;
    @Nullable
    public final String levelUpChannel;
    public final int maxWarnings;
    @Nullable
    public final String modlogChannel;
    @Nullable
    public final GuildSettings.Automod automod;

    public GuildSettings() {
        this.prefix = Config.get("PREFIX");
        this.translationChannels = Collections.emptyList();
        this.isRankingEnabled = true;
        this.levelUpMessage = Config.get("DEFAULT_LEVELUP_MESSAGE");
        this.levelUpChannel = null;
        this.maxWarnings = 3;
        this.modlogChannel = null;
        this.automod = null;
    }

    public GuildSettings(Document doc) {
        this.prefix = doc.get("prefix", Config.get("PREFIX"));
        this.translationChannels = doc.get("translation_channels", new ArrayList<>());
        this.isRankingEnabled = doc.get("ranking_enabled", false);
        this.levelUpMessage = doc.get("levelup_message", Config.get("DEFAULT_LEVELUP_MESSAGE"));
        this.levelUpChannel = doc.getString("levelup_channel");
        this.maxWarnings = doc.get("max_warnings", 3);
        this.modlogChannel = doc.getString("modlog_channel");
        this.automod = new Automod(doc);
    }

    public static class Automod {

        public final int maxMentions, maxRoleMentions;
        public final int maxLines;
        public final boolean preventLinks, preventInvites;
        @Nullable
        public final String logChannel;

        private Automod(Document doc) {
            this.maxMentions = doc.get("max_mentions", 0);
            this.maxRoleMentions = doc.get("max_role_mentions", 0);
            this.maxLines = doc.get("max_lines", 0);
            this.preventLinks = doc.get("anti_links", false);
            this.preventInvites = doc.get("anti_invites", false);
            this.logChannel = doc.getString("automodlog_channel");
        }

    }

}
