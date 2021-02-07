package bot.commands.owner;

import bot.Bot;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.BotUtils;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class ShutDownCommand extends ICommand {

    private final Bot bot;

    public ShutDownCommand(Bot bot) {
        this.name = "shutdown";
        this.category = CommandCategory.OWNER;
        this.bot = bot;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        EventWaiter waiter = null;
        for (Object ev : ctx.getJDA().getEventManager().getRegisteredListeners())
            if (ev instanceof EventWaiter)
                waiter = (EventWaiter) ev;

        if (waiter == null)
            return;

        ctx.reply("Are you sure you want to shutdown the bot? `yes`/`no`");
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(ctx.getAuthor()) && e.getChannel().equals(ctx.getChannel()),
                e -> {
                    final String input = e.getMessage().getContentRaw();
                    if (input.equalsIgnoreCase("yes")) {
                        BotUtils.sendSuccess(e.getMessage());
                        ctx.reply("Shutting down...");
                        final JDA jda = ctx.getEvent().getJDA();
                        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
                        bot.getThreadpool().shutdown();
                        BotCommons.shutdown(jda);
                    } else {
                        ctx.reply("Bot Shutdown cancelled");
                    }
                });

    }

}
