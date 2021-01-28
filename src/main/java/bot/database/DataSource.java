package bot.database;

import bot.database.mongo.MongoDS;
import bot.database.objects.GuildSettings;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public interface DataSource {

    Logger LOGGER = LoggerFactory.getLogger(DataSource.class);
    DataSource INS = new MongoDS();

    GuildSettings getSettings(String guildId);
    void setPrefix(String guildId, String newPrefix);

    // Reaction Role
    void addReactionRole(String guildId, String channelId, String messageId, String roleId, String emote);
    void removeReactionRole(String guildId, String channelId, String messageId, @Nullable String emote);
    @Nullable String getReactionRoleId(String guildId, String channelId, String messageId, String emote);

    // Flag Translations
    void setFlagTranslation(String guildId, boolean isEnabled);
    void updateTranslationChannels(String guildId, List<String> channels);
    void addTranslation(String guildId, String channelId, String messageId, String unicode);
    boolean isTranslated(String guildId, String channelId, String messageId, String unicode);

    // Social & Levelling
    int[] updateXp(Member member, int xp, boolean updateMessages);
    void setReputation(Member member, int rep);
    void setLevel(Member member, int level);
    int[] addCoins(Member member, int coins);
    int[] removeCoins(Member member, int coins);

}