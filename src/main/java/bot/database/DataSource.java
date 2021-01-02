package bot.database;

import bot.database.mongo.MongoDS;
import org.jetbrains.annotations.Nullable;

public interface DataSource {

    DataSource INS = new MongoDS();

    String getPrefix(long guildId);

    void setPrefix(long guildId, String newPrefix);

    void addReactionRole(long guildId, long channelId, long messageId, long roleId, String emote);

    void removeReactionRole(long guildId, long channelId, long messageId, @Nullable String emote) throws Exception;

    long getReactionRoleId(long guildId, long channelId, long messageId, String emote) throws Exception;

}