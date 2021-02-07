package bot;

import bot.data.GreetingType;
import bot.database.DataSource;
import bot.database.objects.CounterConfig;
import bot.database.objects.GuildSettings;
import bot.utils.ImageUtils;
import bot.utils.WebhookUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
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
        jda.getPresence().setActivity(Activity.watching("this server"));

        // Update Counter Channels
        bot.getThreadpool().execute(() -> bot.getMemberHandler().updateCountersOnStartup(jda));

        // Purge CMD & XP-cooldown cache
        bot.getThreadpool().scheduleWithFixedDelay(() -> bot.getXpHandler().cleanCooldowns(),
                0, 1, TimeUnit.DAYS);
        bot.getThreadpool().scheduleWithFixedDelay(() -> bot.getCmdHandler().cleanCooldowns(),
                0, 1, TimeUnit.DAYS);

        // Check Temporary mutes and bans
        bot.getThreadpool().scheduleWithFixedDelay(() -> DataSource.INS.checkTempMutes(jda),
                0, 45, TimeUnit.SECONDS);
        bot.getThreadpool().scheduleWithFixedDelay(() -> DataSource.INS.checkTempBans(jda),
                0, 2, TimeUnit.MINUTES);

    }

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        // Ignore bots and webhook message
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        GuildSettings settings = DataSource.INS.getSettings(event.getGuild().getId());
        String raw = event.getMessage().getContentRaw();

        if (raw.startsWith(settings.prefix)) {
            bot.getCmdHandler().handle(event, settings.prefix);
        }

        if (settings.isRankingEnabled) {
            bot.getThreadpool().execute(() -> bot.getXpHandler().handle(event, settings));
        }

        bot.getAutomodHandler().performAutomod(settings.automod, event.getMessage());

    }

    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (event.getGuild().getOwner() == null || event.getUser().isBot())
            return;

        if (event.getReactionEmote().isEmoji())
            bot.getReactionHandler().handleFlagReaction(event);

        bot.getReactionHandler().handleReactionRole(event, true);

        // Handle Tickets in async
        bot.getThreadpool().execute(() -> bot.getReactionHandler().handleTicket(event));

    }

    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if (event.getUser() == null || event.getUser().isBot() || event.getGuild().getOwner() == null)
            return;

        bot.getReactionHandler().handleReactionRole(event, false);

    }

    private void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final CounterConfig config = DataSource.INS.getCounterConfig(guild.getId());

        if (config != null) {
            if (event.getUser().isBot())
                DataSource.INS.updateBotCount(guild.getId(), true, 1);
            bot.getMemberHandler().handleMemberCounter(guild);
        }

        ImageUtils.sendGreeting(guild, event.getUser(), GreetingType.WELCOME, null);

    }

    private void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        final Guild guild = event.getGuild();
        final CounterConfig config = DataSource.INS.getCounterConfig(guild.getId());

        if (config != null) {
            if (event.getUser().isBot())
                DataSource.INS.updateBotCount(guild.getId(), true, -1);
            bot.getMemberHandler().handleMemberCounter(guild);
        }

        ImageUtils.sendGreeting(guild, event.getUser(), GreetingType.FAREWELL, null);

    }

    private void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        DataSource.INS.removeReactionRole(event.getGuild().getId(), event.getChannel().getId(), event.getMessageId(), null);
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
        } else if (event instanceof GuildMessageReceivedEvent) {
            this.onGuildMessageReceived((GuildMessageReceivedEvent) event);
        } else if (event instanceof GuildMessageReactionAddEvent) {
            this.onGuildMessageReactionAdd((GuildMessageReactionAddEvent) event);
        } else if (event instanceof GuildMessageReactionRemoveEvent) {
            this.onGuildMessageReactionRemove((GuildMessageReactionRemoveEvent) event);
        } else if (event instanceof GuildMemberJoinEvent) {
            this.onGuildMemberJoin((GuildMemberJoinEvent) event);
        } else if (event instanceof GuildMemberRemoveEvent) {
            this.onGuildMemberRemove((GuildMemberRemoveEvent) event);
        } else if (event instanceof GuildMessageDeleteEvent) {
            this.onGuildMessageDelete((GuildMessageDeleteEvent) event);
        }
    }

}
