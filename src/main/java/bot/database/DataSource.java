package bot.database;

import bot.database.mongo.MongoDS;
import bot.database.objects.Economy;
import bot.database.objects.GuildSettings;
import bot.database.objects.WarnLogs;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface DataSource {

    Logger LOGGER = LoggerFactory.getLogger(DataSource.class);
    DataSource INS = new MongoDS();

    // Guild Settings
    GuildSettings getSettings(String guildId);
    void setPrefix(String guildId, String newPrefix);

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

}