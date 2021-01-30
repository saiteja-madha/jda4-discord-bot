package bot;

import bot.database.DataSource;
import bot.database.objects.CounterConfig;
import bot.database.objects.GuildSettings;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
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

    public Listener(Bot bot) {
        this.bot = bot;
    }

    public void onReady(@Nonnull ReadyEvent event) {
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());
        LOGGER.info("Watching {} guilds", event.getJDA().getGuilds().size());
        event.getJDA().getPresence().setActivity(Activity.watching("this server"));

        // Update Counter Channels
        bot.getThreadpool().execute(() -> bot.getMemberHandler().updateCountersOnStartup(event.getJDA()));

        // Purge XP-cooldown cache
        bot.getThreadpool().scheduleWithFixedDelay(() -> bot.getXpHandler().cleanCooldowns(), 0, 1, TimeUnit.DAYS);

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

    }

    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (event.getGuild().getOwner() == null || event.getUser().isBot())
            return;

        if (event.getReactionEmote().isEmoji())
            bot.getReactionHandler().handleFlagReaction(event);

        bot.getReactionHandler().handleReactionRole(event, true);

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

    }

    private void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        final Guild guild = event.getGuild();
        final CounterConfig config = DataSource.INS.getCounterConfig(guild.getId());

        if (config != null) {
            if (event.getUser().isBot())
                DataSource.INS.updateBotCount(guild.getId(), true, -1);
            bot.getMemberHandler().handleMemberCounter(guild);
        }

    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            this.onReady((ReadyEvent) event);
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
        }
    }

}
