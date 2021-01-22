package bot.database;

import bot.database.mongo.MongoDS;
import bot.database.objects.GuildSettings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface DataSource {

    Logger LOGGER = LoggerFactory.getLogger(DataSource.class);
    DataSource INS = new MongoDS();

    GuildSettings getSettings(long guildId);
    void setPrefix(long guildId, String newPrefix);

    // Reaction Role
    void addReactionRole(long guildId, String channelId, String messageId, String roleId, String emote);
    void removeReactionRole(long guildId, String channelId, String messageId, @Nullable String emote);
    String getReactionRoleId(long guildId, String channelId, String messageId, String emote);

    // Flag Translations
    void setFlagTranslation(long guildId, boolean isEnabled);
    void updateTranslationChannels(long guildId, List<String> channels);
    void addTranslation(long guildId, long channelId, long messageId, String unicode);
    boolean isTranslated(long guildId, long channelId, long messageId, String unicode);

}