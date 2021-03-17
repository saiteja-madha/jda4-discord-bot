package bot;

import bot.data.PresenceType;
import bot.database.DataSource;
import bot.utils.BotUtils;
import bot.utils.WebhookUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class Listener implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final Bot bot;
    private final WebhookUtil webhookUtil;

    public Listener(Bot bot) {
        this.bot = bot;
        this.webhookUtil = new WebhookUtil(Config.get("JOIN_LEAVE_WEBHOOK"));
    }

    public void onReady(@Nonnull ReadyEvent event) {
        final JDA jda = event.getJDA();

        LOGGER.info("{} is ready", jda.getSelfUser().getAsTag());
        LOGGER.info("Watching {} guilds", jda.getGuilds().size());

        // Update Presence
        bot.getThreadpool().scheduleWithFixedDelay(() -> BotUtils.updatePresence(jda, PresenceType.MEMBERS),
                0, 30, TimeUnit.MINUTES);

        // Update Counter Channels
        bot.getThreadpool().execute(() -> bot.getMemberHandler().updateCountersOnStartup(jda));

        // Purge CMD & XP-cooldown cache
        bot.getThreadpool().scheduleWithFixedDelay(() -> bot.getXpHandler().cleanCooldowns(),
                1, 1, TimeUnit.DAYS);
        bot.getThreadpool().scheduleWithFixedDelay(() -> bot.getCmdHandler().cleanCooldowns(),
                1, 1, TimeUnit.DAYS);

        // Check Temporary mutes and bans
        bot.getThreadpool().scheduleWithFixedDelay(() -> DataSource.INS.checkTempMutes(jda),
                0, 45, TimeUnit.SECONDS);
        bot.getThreadpool().scheduleWithFixedDelay(() -> DataSource.INS.checkTempBans(jda),
                0, 2, TimeUnit.MINUTES);

    }

    private void onGuildJoin(@Nonnull GuildJoinEvent event) {
        final Guild guild = event.getGuild();
        guild.retrieveOwner().queue((owner) -> {
            LOGGER.info("Guild Joined - GuildID: {} | OwnerId: {} | Members: {}", guild.getId(), owner.getId(), guild.getMemberCount());
            webhookUtil.sendWebhook(owner, guild, WebhookUtil.Action.JOIN);
            DataSource.INS.registerGuild(guild, owner);
        });
    }

    private void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        final Guild guild = event.getGuild();
        guild.retrieveOwner().queue((owner) -> {
            LOGGER.info("Guild Left - GuildID: {} | OwnerId: {} | Members: {}", guild.getId(), owner.getId(), guild.getMemberCount());
            webhookUtil.sendWebhook(owner, guild, WebhookUtil.Action.LEAVE);
        });
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            this.onReady((ReadyEvent) event);
        } else if (event instanceof GuildJoinEvent) {
            this.onGuildJoin((GuildJoinEvent) event);
        } else if (event instanceof GuildLeaveEvent) {
            this.onGuildLeave((GuildLeaveEvent) event);
        }
    }

}
