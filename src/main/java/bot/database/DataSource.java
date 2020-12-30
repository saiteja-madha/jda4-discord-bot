package bot.database;

import bot.database.sqlite.SQLiteDS;
import com.mongodb.lang.Nullable;

public interface DataSource {

    DataSource INS = new SQLiteDS();

    String getPrefix(long guildId);

    void setPrefix(long guildId, String newPrefix);

    void addReactionRole(long guildId, long channelId, long messageId, long roleId, String emote);

    void removeReactionRole(long guildId, long channelId, long messageId, @Nullable String emote) throws Exception;

    long getReactionRoleId(long guildId, long channelId, long messageId, String emote) throws Exception;

}