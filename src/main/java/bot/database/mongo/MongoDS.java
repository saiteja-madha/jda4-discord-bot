package bot.database.mongo;

import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.main.Config;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MongoDS implements DataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDS.class);
    private final MongoClient mongoClient;

    // Caching
    private final Map<Long, GuildSettings> settings = new HashMap<>();

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
    public GuildSettings getSettings(long guildId) {
        if (!settings.containsKey(guildId))
            settings.put(guildId, getSettingsCache(guildId));
        return settings.get(guildId);
    }

    @Override
    public void setPrefix(long guildId, String newPrefix) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_Settings");
        Bson filter = Filters.eq("guild_id", guildId);
        collection.updateOne(filter, Updates.set("prefix", newPrefix), new UpdateOptions().upsert(true));
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

    @NotNull
    private GuildSettings getSettingsCache(long guildId) {
        Bson filter = Filters.eq("guild_id", guildId);
        MongoCollection<Document> settingsCollection = mongoClient.getDatabase("discord").getCollection("guild_Settings");
        Document document = settingsCollection.find(filter).first();
        if (document == null)
            return new GuildSettings();
        else
            return new GuildSettings((String) document.get("prefix"));
    }

}
