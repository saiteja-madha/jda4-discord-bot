package bot.handlers;

import bot.Bot;
import bot.data.CounterType;
import bot.database.DataSource;
import bot.database.objects.CounterConfig;
import bot.utils.GuildUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MemberHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(MemberHandler.class);
    // Prevent Rate-limiting
    private final Set<String> counterProgress = new HashSet<>();
    private final Bot bot;

    public MemberHandler(Bot bot) {
        this.bot = bot;
    }

    public void handleMemberCounter(Guild guild) {

        // If update is already scheduled - Skip GUILD
        if (counterProgress.contains(guild.getId())) {
            return;
        }

        // Add guildId to in-progress guilds
        counterProgress.add(guild.getId());

        // Schedule channel name update
        bot.getThreadpool().schedule(() -> {
            final CounterConfig config = DataSource.INS.getCounterConfig(guild.getId());
            final int botCount = config.botCount;
            final int totalCount = guild.getMemberCount();
            final int memCount = totalCount - botCount;

            try {

                if (config.tCountChannel != null) {
                    VoiceChannel vc = GuildUtils.getVoiceChannelById(guild, config.tCountChannel);
                    if (vc == null)
                        DataSource.INS.setCounter(CounterType.ALL, guild, null, null);
                    else {
                        String name = config.tCountName + " : " + totalCount;
                        GuildUtils.setVoiceChannelName(vc, name);
                    }
                }

                if (config.bCountChannel != null) {
                    VoiceChannel vc = GuildUtils.getVoiceChannelById(guild, config.bCountChannel);
                    if (vc == null)
                        DataSource.INS.setCounter(CounterType.BOTS, guild, null, null);
                    else {
                        String name = config.bCountName + " : " + botCount;
                        GuildUtils.setVoiceChannelName(vc, name);
                    }
                }

                if (config.mCountChannel != null) {
                    VoiceChannel vc = GuildUtils.getVoiceChannelById(guild, config.mCountChannel);
                    if (vc == null)
                        DataSource.INS.setCounter(CounterType.MEMBERS, guild, null, null);
                    else {
                        String name = config.mCountName + " : " + memCount;
                        GuildUtils.setVoiceChannelName(vc, name);
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Error Updating counter channel: {}", e.getMessage());
            } finally {
                counterProgress.remove(guild.getId());
            }

        }, 5, TimeUnit.MINUTES);

    }

    public void updateCountersOnStartup(JDA jda) {
        final List<String> guildIds = DataSource.INS.getCounterGuilds();
        for (String guildId : guildIds) {
            final Guild guild = jda.getGuildById(guildId);

            // Maybe bot has left the guild
            if (guild == null) {
                continue;
            }

            GuildUtils.getMemberStats(guild, data -> {
                int bots = (int) data[1];
                DataSource.INS.updateBotCount(guildId, false, bots);
                handleMemberCounter(guild);
            });

        }

        LOGGER.info("Total counter-enabled guild(s): {}", guildIds.size());

    }

}
