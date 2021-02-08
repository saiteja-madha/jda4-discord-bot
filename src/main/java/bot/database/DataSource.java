package bot.database;

import bot.data.CounterType;
import bot.data.GreetingType;
import bot.database.mongo.MongoDS;
import bot.database.objects.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public interface DataSource {

    Logger LOGGER = LoggerFactory.getLogger(DataSource.class);
    DataSource INS = new MongoDS();

    // Guild Settings
    GuildSettings getSettings(String guildId);
    void setPrefix(String guildId, String newPrefix);
    void xpSystem(String guildId, boolean isEnabled);
    void setMaxWarnings(String guildId, int warnings);
    void setModLogChannel(String guildId, @Nullable String logChannel);

    // Automod Settings
    void setAutomodLogChannel(String guildId, @Nullable String channelId);
    void antiInvites(String guildId, boolean isEnabled);
    void antiLinks(String guildId, boolean isEnabled);
    void setMaxLines(String guildId, int count);
    void setMaxMentions(String guildId, int count);
    void setMaxRoleMentions(String guildId, int count);

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
    void setReputation(Member member, int rep);
    void setLevel(Member member, int level);
    int[] updateXp(Member member, int xp, boolean updateMessages); // [oldLevel, oldXp, oldMessages]

    // Economy
    Economy getEconomy(Member member);
    int[] addCoins(Member member, int coins); // [oldCoins, newCoins]
    int[] removeCoins(Member member, int coins); // [oldCoins, newCoins]
    int[] updateDailyStreak(Member member, int coins, int streak); // [oldCoins, newCoins]

    // Moderation
    void warnUser(Member mod, Member target, String reason);
    void deleteWarnings(Member target);
    List<WarnLogs> getWarnLogs(Member member);
    void tempMute(String guildId, String memberId, Instant unmuteTime);
    void tempBan(String guildId, String memberId, Instant unbanTime);
    void checkTempMutes(JDA jda);
    void checkTempBans(JDA jda);

    // Counter Channels
    List<String> getCounterGuilds();
    CounterConfig getCounterConfig(String guildId);
    void updateBotCount(String guildId, boolean isIncrement, int count);
    void setCounter(CounterType type, Guild guild, @Nullable VoiceChannel vc, @Nullable String name);

    // Ticket
    void addTicketConfig(String guildId, String channelId, String messageId, String title, String roleId);
    @Nullable Ticket getTicketConfig(String guildId);
    void setTicketLogChannel(String guildId, String channelId);
    void setTicketLimit(String guildId, int limit);
    void setTicketClose(String guildId, boolean isAdminOnly);
    void deleteTicketConfig(String guildId);

    // Welcome & Farewell Data
    @Nullable Greeting getWelcomeConfig(String guildId);
    @Nullable Greeting getFarewellConfig(String guildId);
    void setGreetingChannel(String guildId, @Nullable String channelId, GreetingType type);

    // Welcome & Farewell Embed
    void enableGreetingEmbed(String guildId, boolean enabled, GreetingType type);
    void setGreetingDesc(String guildId, @Nullable String description, GreetingType type);
    void setGreetingFooter(String guildId, @Nullable String description, GreetingType type);
    void setGreetingColor(String guildId, String color, GreetingType type);

    // Welcome & Farewell Image
    void enableGreetingImage(String guildId, boolean enabled, GreetingType type);
    void setGreetingImageMsg(String guildId, @Nullable String message, GreetingType type);
    void setGreetingImageBkg(String guildId, @Nullable String bkg, GreetingType type);

    // Guild Data
    void registerGuild(Guild guild, Member owner);

}