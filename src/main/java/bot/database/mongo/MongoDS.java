package bot.database.mongo;

import bot.Config;
import bot.database.DataSource;
import bot.database.objects.Economy;
import bot.database.objects.GuildSettings;
import bot.database.objects.WarnLogs;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import net.dv8tion.jda.api.entities.Member;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoDS implements DataSource {

    private final MongoClient mongoClient;

    // Caching
    private final Map<String, GuildSettings> settings = new HashMap<>();

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
    public GuildSettings getSettings(String guildId) {
        if (!settings.containsKey(guildId))
            settings.put(guildId, fetchSettings(guildId));
        return settings.get(guildId);
    }

    @Override
    public void setPrefix(String guildId, String newPrefix) {
        invalidateCache(guildId);
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_settings");
        Bson filter = Filters.eq("_id", guildId);
        collection.updateOne(filter, Updates.set("prefix", newPrefix), new UpdateOptions().upsert(true));
    }

    @Override
    public void addReactionRole(String guildId, String channelId, String messageId, String roleId, String emote) {
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
    public void removeReactionRole(String guildId, String channelId, String messageId, @Nullable String emote) {
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

    @Override
    public @Nullable String getReactionRoleId(String guildId, String channelId, String messageId, String emote) {
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
    public void setFlagTranslation(String guildId, boolean isEnabled) {
        invalidateCache(guildId);
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_settings");
        Bson filter = Filters.eq("_id", guildId);
        collection.updateOne(filter, Updates.set("flag_translation", isEnabled), new UpdateOptions().upsert(true));
    }

    @Override
    public void updateTranslationChannels(String guildId, List<String> channels) {
        invalidateCache(guildId);
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_settings");
        Bson filter = Filters.eq("_id", guildId);
        collection.updateOne(filter, Updates.set("translation_channels", channels), new UpdateOptions().upsert(true));
    }

    @Override
    public void addTranslation(String guildId, String channelId, String messageId, String unicode) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("translations");
        Document doc = new Document("_id", new ObjectId());
        doc.append("guild_id", guildId)
                .append("channel_id", channelId)
                .append("message_id", messageId)
                .append("unicode", unicode);
        collection.insertOne(doc);
    }

    @Override
    public boolean isTranslated(String guildId, String channelId, String messageId, String unicode) {
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

    @Override
    public int[] updateXp(Member member, int xp, boolean updateMessages) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("levels");

        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );

        Bson update = (updateMessages)
                ? Updates.combine(Updates.inc("xp", xp), Updates.inc("messages", 1))
                : Updates.inc("xp", xp);

        Document prevDoc = collection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.BEFORE));

        if (prevDoc == null)
            return new int[]{0, 0, 0};
        else
            return new int[]{prevDoc.containsKey("level") ? prevDoc.getInteger("level") : 0,
                    prevDoc.getInteger("xp"),
                    prevDoc.getInteger("messages")
            };
    }

    @Override
    public void setReputation(Member member, int rep) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("levels");
        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );
        collection.updateOne(filter, Updates.inc("reputation", rep), new UpdateOptions().upsert(true));
    }

    @Override
    public void setLevel(Member member, int level) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("levels");
        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );
        collection.updateOne(filter, Updates.set("level", level), new UpdateOptions().upsert(true));
    }

    @Override
    public Economy getEconomy(Member member) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("economy");
        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );
        Document document = collection.find(filter).first();
        return (document == null) ? new Economy() : new Economy(document);
    }

    @Override
    public int[] addCoins(Member member, int coins) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("economy");

        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );

        Document updateQuery = new Document()
                .append("$inc", new Document().append("coins", coins));

        Document oldDoc = collection.findOneAndUpdate(filter, updateQuery, new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.BEFORE));

        if (oldDoc == null)
            return new int[]{0, coins};
        else {
            int oldCoins = oldDoc.containsKey("coins") ? oldDoc.getInteger("coins") : 0;
            return new int[]{oldCoins, (oldCoins + coins)};
        }

    }

    @Override
    public int[] removeCoins(Member member, int coins) {
        return this.addCoins(member, -1 * coins);
    }

    @Override
    public int[] updateDailyStreak(Member member, int coins, int streak) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("economy");

        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );

        Bson update = Updates.combine(
                Updates.set("daily_timestamp", Instant.now()),
                Updates.set("daily_streak", streak),
                Updates.inc("coins", coins)
        );

        Document oldDoc = collection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.BEFORE));

        if (oldDoc == null)
            return new int[]{0, coins};
        else {
            int oldCoins = oldDoc.containsKey("coins") ? oldDoc.getInteger("coins") : 0;
            return new int[]{oldCoins, (oldCoins + coins)};
        }
    }

    @Override
    public void warnUser(Member mod, Member target, String reason) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("warn_logs");
        Document doc = new Document()
                .append("guild_id", mod.getGuild().getId())
                .append("moderator_id", mod.getId())
                .append("moderator_name", mod.getUser().getAsTag())
                .append("member_id", target.getId())
                .append("reason", reason)
                .append("time_stamp", Instant.now());

        collection.insertOne(doc);
    }

    @Override
    public void deleteWarnings(Member member) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("warn_logs");
        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );
        collection.deleteMany(filter);
    }

    @Override
    public List<WarnLogs> getWarnLogs(Member member) {
        List<WarnLogs> list = new ArrayList<>();
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("warn_logs");
        Bson filter = Filters.and(
                Filters.eq("guild_id", member.getGuild().getId()),
                Filters.eq("member_id", member.getId())
        );
        FindIterable<Document> documents = collection.find(filter);
        for (Document doc : documents) {
            list.add(new WarnLogs(doc));
        }
        return list;
    }

    @NotNull
    private GuildSettings fetchSettings(String guildId) {
        MongoCollection<Document> settingsCollection = mongoClient.getDatabase("discord").getCollection("guild_settings");
        Bson filter = Filters.eq("_id", guildId);
        Document document = settingsCollection.find(filter).first();
        return (document == null) ? new GuildSettings() : new GuildSettings(document);
    }

    private void invalidateCache(String guildId) {
        settings.remove(guildId);
    }

}
