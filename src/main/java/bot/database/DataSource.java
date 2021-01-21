package bot.database;

import bot.database.mongo.MongoDS;
import bot.database.objects.GuildSettings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DataSource {

    Logger LOGGER = LoggerFactory.getLogger(DataSource.class);

    DataSource INS = new MongoDS();

    GuildSettings getSettings(long guildId);

    void setPrefix(long guildId, String newPrefix);

    void addReactionRole(long guildId, long channelId, long messageId, long roleId, String emote);

    void removeReactionRole(long guildId, long channelId, long messageId, @Nullable String emote) throws Exception;

    long getReactionRoleId(long guildId, long channelId, long messageId, String emote) throws Exception;

}