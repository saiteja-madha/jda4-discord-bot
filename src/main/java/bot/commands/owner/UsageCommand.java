package bot.commands.owner;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.handlers.CommandHandler;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class UsageCommand extends ICommand {

    private final Paginator.Builder pBuilder;

    public UsageCommand(EventWaiter waiter) {
        this.name = "usage";
        this.help = "shows command usage";
        this.category = CommandCategory.OWNER;
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_HISTORY,
                Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MANAGE};
        this.pBuilder = new Paginator.Builder()
                .setColumns(3).setItemsPerPage(40)
                .setEventWaiter(waiter)
                .setFinalAction(m -> m.clearReactions().queue(null, null))
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            pBuilder.clearItems();
            final CommandHandler manager = ctx.getCmdHandler();

            manager.getCommands().stream()
                    .map(c -> Constants.ARROW + " " + c.getName() + ": `" + manager.getCommandUses(c.getName()) + "`")
                    .forEach(pBuilder::addItems);

            Paginator p = pBuilder.setColor(Color.decode(String.valueOf(Constants.BOT_EMBED)))
                    .setUsers(ctx.getAuthor())
                    .setText("**Command Usage**")
                    .build();

            p.paginate(ctx.getChannel(), 1);
            return;
        }

        final String cmdName = ctx.getArgs().get(0);
        int commandUses = ctx.getCmdHandler().getCommandUses(cmdName);

        ctx.reply(String.format("%s: %s times", cmdName, commandUses));

    }

}
