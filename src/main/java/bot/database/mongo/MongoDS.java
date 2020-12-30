package bot.database.mongo;

import bot.database.DataSource;
import bot.main.Config;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class MongoDS implements DataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDS.class);
    private final MongoClient mongoClient;

    public MongoDS() {
        ConnectionString connString = new ConnectionString(Config.get("MONGO_CONNECTION_STRING"));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        mongoClient = MongoClients.create(settings);
        LOGGER.info("MongoDB successfully initialized");
    }

    @Override
    public String getPrefix(long guildId) {
        return null;
    }

    @Override
    public void setPrefix(long guildId, String newPrefix) {

    }

    @Override
    public void addReactionRole(long guildId, long channelId, long messageId, long roleId, String emote) {

    }

    @Override
    public void removeReactionRole(long guildId, long channelId, long messageId, @Nullable String emote) {

    }

    @Override
    public long getReactionRoleId(long guildId, long channelId, long messageId, String emote) {
        return 0;
    }

}
