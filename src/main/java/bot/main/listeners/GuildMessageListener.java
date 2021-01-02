package bot.main.listeners;

import bot.database.DataSource;
import bot.main.CommandManager;
import bot.main.Config;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class GuildMessageListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuildMessageListener.class);
    private final CommandManager manager = new CommandManager();

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();

        if (user.isBot() || event.isWebhookMessage()) {
            return;
        }

        final long guildId = event.getGuild().getIdLong();
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
            manager.handle(event, prefix);
        }
    }

}
