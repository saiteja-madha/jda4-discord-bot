package bot.database.mongo;

import bot.Config;
import bot.data.CounterType;
import bot.data.GreetingType;
import bot.database.DataSource;
import bot.database.objects.*;
import bot.utils.FixedSizeCache;
import bot.utils.GuildUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MongoDS implements DataSource {

    private final MongoClient mongoClient;

    // Caching
    private final FixedSizeCache<String, GuildSettings> settingsCache = new FixedSizeCache<>(Config.getInt("CACHE_SIZE"));
    private final FixedSizeCache<String, CounterConfig> counterCache = new FixedSizeCache<>(Config.getInt("CACHE_SIZE"));
    private final FixedSizeCache<String, Greeting.Welcome> welcomeCache = new FixedSizeCache<>(Config.getInt("CACHE_SIZE"));
    private final FixedSizeCache<String, Greeting.Farewell> farewellCache = new FixedSizeCache<>(Config.getInt("CACHE_SIZE"));

    public MongoDS() {

        // Configure MongoDB Log Level
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.WARN);

        ConnectionString connString = new ConnectionString(Config.get("MONGO_CONNECTION_STRING"));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        mongoClient = MongoClients.create(settings);
        LOGGER.info("MongoDB successfully initialized");
    }

    private @NotNull GuildSettings fetchSettings(String guildId) {
        MongoCollection<Document> settingsCollection = mongoClient.getDatabase("discord").getCollection("guild_settings");
        Bson filter = Filters.eq("_id", guildId);
        Document document = settingsCollection.find(filter).first();
        return (document == null) ? new GuildSettings() : new GuildSettings(document);
    }

    private void updateSettings(String guildId, String key, Object value) {
        settingsCache.remove(guildId);
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_settings");
        Bson filter = Filters.eq("_id", guildId);
        Bson update = Updates.set(key, value);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    @Override
    public GuildSettings getSettings(String guildId) {
        if (!settingsCache.contains(guildId))
            settingsCache.put(guildId, fetchSettings(guildId));
        return settingsCache.get(guildId);
    }

    @Override
    public void setPrefix(String guildId, String newPrefix) {
        updateSettings(guildId, "prefix", newPrefix);
    }

    @Override
    public void xpSystem(String guildId, boolean isEnabled) {
        updateSettings(guildId, "ranking_enabled", isEnabled);
    }

    @Override
    public void setMaxWarnings(String guildId, int warnings) {
        updateSettings(guildId, "max_warnings", warnings);
    }

    @Override
    public void setModLogChannel(String guildId, String logChannel) {
        updateSettings(guildId, "modlog_channel", logChannel);
    }

    @Override
    public void updateTranslationChannels(String guildId, List<String> channels) {
        updateSettings(guildId, "translation_channels", channels);
    }

    @Override
    public void setAutomodLogChannel(String guildId, @Nullable String channelId) {
        updateSettings(guildId, "automodlog_channel", channelId);
    }

    @Override
    public void antiInvites(String guildId, boolean isEnabled) {
        updateSettings(guildId, "anti_invites", isEnabled);
    }

    @Override
    public void antiLinks(String guildId, boolean isEnabled) {
        updateSettings(guildId, "anti_links", isEnabled);
    }

    @Override
    public void setMaxLines(String guildId, int count) {
        updateSettings(guildId, "max_lines", count);
    }

    @Override
    public void setMaxMentions(String guildId, int count) {
        updateSettings(guildId, "max_mentions", count);
    }

    @Override
    public void setMaxRoleMentions(String guildId, int count) {
        updateSettings(guildId, "max_role_mentions", count);
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
    public void addTranslation(String guildId, String channelId, String messageId, String unicode) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("translate_logs");
        Document doc = new Document("_id", new ObjectId());
        doc.append("guild_id", guildId)
                .append("channel_id", channelId)
                .append("message_id", messageId)
                .append("unicode", unicode);
        collection.insertOne(doc);
    }

    @Override
    public boolean isTranslated(String guildId, String channelId, String messageId, String unicode) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("translate_logs");
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
    public int[] incrementXp(Member member, int xp, boolean updateMessages) {
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
            return new int[]{prevDoc.getInteger("level", 0),
                    prevDoc.getInteger("xp", 0),
                    prevDoc.getInteger("messages", 0)
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

    @Override
    public void tempMute(String guildId, String memberId, Instant unmuteTime) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("tempmute_logs");
        Document doc = new Document()
                .append("guild_id", guildId)
                .append("member_id", memberId)
                .append("unmute_time", unmuteTime);

        collection.insertOne(doc);
    }

    @Override
    public void tempBan(String guildId, String memberId, Instant unbanTime) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("tempban_logs");
        Document doc = new Document()
                .append("guild_id", guildId)
                .append("member_id", memberId)
                .append("unban_time", unbanTime);

        collection.insertOne(doc);
    }

    @Override
    public void checkTempMutes(JDA jda) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("tempmute_logs");
        Bson filter = Filters.gt("unmute_time", Instant.now());
        FindIterable<Document> documents = collection.find(filter);
        for (Document doc : documents) {
            final String guildId = doc.getString("guild_id");
            final String memberId = doc.getString("member_id");
            final Guild guild = jda.getGuildById(guildId);
            if (guild == null || !guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))
                continue;
            guild.retrieveMemberById(memberId).queue((m) -> {
                Role mutedRole = GuildUtils.getMutedRole(guild);
                if (mutedRole != null && m.getRoles().contains(mutedRole)) {
                    guild.removeRoleFromMember(m, mutedRole).reason("TempMute completed").queue(
                            (__) -> {
                                Bson delFilter = Filters.and(
                                        Filters.eq("guild_id", guildId),
                                        Filters.eq("member_id", memberId)
                                );
                                collection.deleteOne(delFilter);
                            }
                    );
                }
            }, e -> LOGGER.error("Retrieve Member Failed - GuildId: " + guildId + " MemberId: " + memberId
                    + " Error: " + e.getMessage()));
        }
    }

    @Override
    public void checkTempBans(JDA jda) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("tempban_logs");
        Bson filter = Filters.gt("unban_time", Instant.now());
        FindIterable<Document> documents = collection.find(filter);
        for (Document doc : documents) {
            final String guildId = doc.getString("guild_id");
            final String memberId = doc.getString("member_id");
            final Guild guild = jda.getGuildById(guildId);
            if (guild == null || !guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))
                continue;

            guild.unban(memberId).queue((__) -> {
                Bson delFilter = Filters.and(
                        Filters.eq("guild_id", guildId),
                        Filters.eq("member_id", memberId)
                );
                collection.deleteOne(delFilter);
            });

        }
    }

    @Override
    public List<String> getCounterGuilds() {
        List<String> list = new ArrayList<>();
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("counter_config");
        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            list.add(document.getString("_id"));
        }
        return list;
    }

    private CounterConfig fetchCounterConfig(String guildId) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("counter_config");
        Bson filter = Filters.eq("_id", guildId);
        Document document = collection.find(filter).first();
        return (document == null) ? new CounterConfig() : new CounterConfig(document);
    }

    @Override
    public CounterConfig getCounterConfig(String guildId) {
        if (!counterCache.contains(guildId))
            counterCache.put(guildId, fetchCounterConfig(guildId));
        return counterCache.get(guildId);
    }

    @Override
    public void updateBotCount(String guildId, boolean isIncrement, int count) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("counter_config");
        Bson filter = Filters.eq("_id", guildId);
        Bson update = (isIncrement) ? Updates.inc("bot_count", count) : Updates.set("bot_count", count);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void setCounter(CounterType type, Guild guild, @Nullable VoiceChannel vc, String name) {
        String var1, var2;

        switch (type) {
            case ALL:
                var1 = "total_count_channel";
                var2 = "total_count_name";
                break;

            case MEMBERS:
                var1 = "member_count_channel";
                var2 = "member_count_name";
                break;

            case BOTS:
                var1 = "bot_count_channel";
                var2 = "bot_count_name";
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("counter_config");
        Bson filter = Filters.eq("_id", guild.getId());
        Bson update = Updates.combine(
                Updates.set(var1, vc == null ? null : vc.getId()),
                Updates.set(var2, name)
        );

        collection.updateOne(filter, update, new UpdateOptions().upsert(true));

    }

    @Override
    public void addTicketConfig(String guildId, String channelId, String messageId, String title, String roleId) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("ticket_config");
        Document doc = new Document()
                .append("guild_id", guildId)
                .append("channel_id", channelId)
                .append("message_id", messageId)
                .append("title", title)
                .append("support_role", roleId);

        collection.insertOne(doc);
    }

    @Override
    public Ticket getTicketConfig(String guildId) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("ticket_config");
        Bson filter = Filters.eq("guild_id", guildId);
        Document document = collection.find(filter).first();
        return document == null ? null : new Ticket(document);
    }

    @Override
    public void setTicketLogChannel(String guildId, String logchannel) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("ticket_config");
        Bson filter = Filters.eq("guild_id", guildId);
        Bson update = Updates.set("log_channel", logchannel);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void setTicketLimit(String guildId, int limit) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("ticket_config");
        Bson filter = Filters.eq("guild_id", guildId);
        Bson update = Updates.set("ticket_limit", limit);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void setTicketClose(String guildId, boolean isAdminOnly) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("ticket_config");
        Bson filter = Filters.eq("guild_id", guildId);
        Bson update = Updates.set("admin_only", isAdminOnly);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void deleteTicketConfig(String guildId) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("ticket_config");
        Bson filter = Filters.eq("guild_id", guildId);
        collection.deleteOne(filter);
    }

    private @Nullable Greeting.Welcome fetchWelcomeConfig(String guildId) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("welcome_config");
        Bson filter = Filters.eq("guild_id", guildId);
        Document document = collection.find(filter).first();
        return document == null ? null : new Greeting.Welcome(document);
    }

    private @Nullable Greeting.Farewell fetchFarewellConfig(String guildId) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("farewell_config");
        Bson filter = Filters.eq("guild_id", guildId);
        Document document = collection.find(filter).first();
        return document == null ? null : new Greeting.Farewell(document);
    }

    @Override
    public Greeting.Welcome getWelcomeConfig(String guildId) {
        if (!welcomeCache.contains(guildId))
            welcomeCache.put(guildId, fetchWelcomeConfig(guildId));
        return welcomeCache.get(guildId);
    }

    @Override
    public Greeting.Farewell getFarewellConfig(String guildId) {
        if (!farewellCache.contains(guildId))
            farewellCache.put(guildId, fetchFarewellConfig(guildId));
        return farewellCache.get(guildId);
    }

    private void updateGreeting(String guildId, GreetingType type, String key, Object value) {
        MongoCollection<Document> collection;
        if (type == GreetingType.WELCOME) {
            collection = mongoClient.getDatabase("discord").getCollection("welcome_config");
            welcomeCache.remove(guildId);
        } else {
            collection = mongoClient.getDatabase("discord").getCollection("farewell_config");
            farewellCache.remove(guildId);
        }
        Bson filter = Filters.eq("guild_id", guildId);
        Bson update = Updates.set(key, value);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void setGreetingChannel(String guildId, @Nullable String channelId, GreetingType type) {
        this.updateGreeting(guildId, type, "channel_id", channelId);
    }

    @Override
    public void setGreetingDesc(String guildId, @Nullable String description, GreetingType type) {
        this.updateGreeting(guildId, type, "description", description);
    }

    @Override
    public void setGreetingFooter(String guildId, @Nullable String footer, GreetingType type) {
        this.updateGreeting(guildId, type, "embed_footer", footer);
    }

    @Override
    public void setGreetingColor(String guildId, String color, GreetingType type) {
        this.updateGreeting(guildId, type, "embed_color", color);
    }

    @Override
    public void setGreetingThumbnail(String guildId, boolean enabled, GreetingType type) {
        this.updateGreeting(guildId, type, "embed_thumbnail", enabled);
    }

    @Override
    public void setGreetingImage(String guildId, String image, GreetingType type) {
        this.updateGreeting(guildId, type, "embed_image", image);
    }

    @Override
    public void registerGuild(Guild guild, Member owner) {
        MongoCollection<Document> collection = mongoClient.getDatabase("discord").getCollection("guild_data");
        Document doc = new Document()
                .append("guild_id", guild.getId())
                .append("guild_name", guild.getName())
                .append("guild_region", guild.getRegion().getName())
                .append("guild_ownerId", owner.getId())
                .append("guild_owner", owner.getUser().getAsTag())
                .append("join_timestamp", Instant.now());

        collection.insertOne(doc);
    }

}
