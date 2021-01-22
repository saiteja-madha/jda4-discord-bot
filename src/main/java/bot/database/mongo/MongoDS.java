package bot.database.mongo;

import bot.Config;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
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
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoDS implements DataSource {

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
            settings.put(guildId, fetchSettings(guildId));
        return settings.get(guildId);
    }

    @Override
    public void setPrefix(long guildId, String newPrefix) {
        invalidateCache(guildId);
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_Settings");
        Bson filter = Filters.eq("_id", guildId);
        collection.updateOne(filter, Updates.set("prefix", newPrefix), new UpdateOptions().upsert(true));
    }

    @Override
    public void addReactionRole(long guildId, String channelId, String messageId, String roleId, String emote) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("reaction_roles");

        Bson updates = Updates.combine(Updates.set("guild_id", guildId),
                Updates.set("channel_id", channelId),
                Updates.set("message_id", messageId),
                Updates.set("channel_id", channelId)
        );

        Bson filter = Filters.and(
                Filters.eq("guild_id", guildId),
                Filters.eq("channel_id", channelId),
                Filters.eq("role_id", roleId),
                Filters.eq("emote", emote)
        );

        collection.updateOne(filter, updates, new UpdateOptions().upsert(true));
    }

    @Override
    public void removeReactionRole(long guildId, String channelId, String messageId, @Nullable String emote) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("reaction_roles");
        Bson filter = Filters.and(
                Filters.eq("guild_id", guildId),
                Filters.eq("channel_id", channelId),
                Filters.eq("message_id", messageId)
        );

        if (emote != null)
            filter = Filters.and(filter, Filters.eq("emote", emote));
        collection.deleteMany(filter);
    }

    @Nullable
    @Override
    public String getReactionRoleId(long guildId, String channelId, String messageId, String emote) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("reaction_roles");

        Bson filter = Filters.and(
                Filters.eq("guild_id", guildId),
                Filters.eq("channel_id", channelId),
                Filters.eq("emote", emote)
        );

        Document doc = collection.find(filter).first();
        return (doc == null) ? null : doc.getString("role_id");

    }

    @Override
    public void setFlagTranslation(long guildId, boolean isEnabled) {
        invalidateCache(guildId);
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_Settings");
        Bson filter = Filters.eq("_id", guildId);
        collection.updateOne(filter, Updates.set("flag_translation", isEnabled), new UpdateOptions().upsert(true));
    }

    @Override
    public void updateTranslationChannels(long guildId, List<String> channels) {
        invalidateCache(guildId);
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_Settings");
        Bson filter = Filters.eq("_id", guildId);
        collection.updateOne(filter, Updates.set("translation_channels", channels), new UpdateOptions().upsert(true));
    }

    @Override
    public void addTranslation(long guildId, long channelId, long messageId, String unicode) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("translations");
        Document doc = new Document("_id", new ObjectId());
        doc.append("guild_id", guildId)
                .append("channel_id", channelId)
                .append("message_id", messageId)
                .append("unicode", unicode);
        collection.insertOne(doc);
    }

    @Override
    public boolean isTranslated(long guildId, long channelId, long messageId, String unicode) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("translations");
        Bson filter = Filters.and(
                Filters.eq("guild_id", guildId),
                Filters.eq("channel_id", guildId),
                Filters.eq("message_id", guildId),
                Filters.eq("unicode", unicode)
        );
        Document document = collection.find(filter).first();
        return document != null;
    }

    @NotNull
    private GuildSettings fetchSettings(long guildId) {
        Bson filter = Filters.eq("_id", guildId);
        MongoCollection<Document> settingsCollection = mongoClient.getDatabase("discord").getCollection("guild_Settings");
        Document document = settingsCollection.find(filter).first();
        return (document == null) ? new GuildSettings() : new GuildSettings(document);
    }

    private void invalidateCache(long guildId) {
        settings.remove(guildId);
    }

}
