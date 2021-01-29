package bot;

import bot.database.DataSource;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
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

        // purge XP-cooldown cache
        bot.getThreadpool().schedule(() -> bot.getXpHandler().cleanCooldowns(), 1, TimeUnit.DAYS);

    }

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();

        if (user.isBot() || event.isWebhookMessage()) {
            return;
        }

        final String guildId = event.getGuild().getId();
        String prefix = DataSource.INS.getSettings(guildId).prefix;

        String raw = event.getMessage().getContentRaw();

        if (raw.equalsIgnoreCase(prefix + "shutdown")
                && user.getId().equals(Config.get("owner_id"))) {
            LOGGER.info("Shutting down");
            event.getJDA().shutdown();
            BotCommons.shutdown(event.getJDA());
            return;
        }

        if (raw.startsWith(prefix)) {
            bot.getCmdHandler().handle(event, prefix);
        }

        bot.getThreadpool().execute(() -> bot.getXpHandler().handle(event));

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

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            this.onReady((ReadyEvent) event);
        } else if (event instanceof GuildMessageReceivedEvent) {
            this.onGuildMessageReceived((GuildMessageReceivedEvent) event);
        } else if (event instanceof GuildMessageReactionAddEvent) {
            bot.getThreadpool().execute(() -> this.onGuildMessageReactionAdd((GuildMessageReactionAddEvent) event));
        } else if (event instanceof GuildMessageReactionRemoveEvent) {
            bot.getThreadpool().execute(() -> this.onGuildMessageReactionRemove((GuildMessageReactionRemoveEvent) event));
        }
    }

}
