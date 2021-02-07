package bot.commands.automod;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class AntiLinksCommand extends ICommand {

    public AntiLinksCommand() {
        this.name = "antilinks";
        this.minArgsCount = 1;
        this.usage = "<ON | OFF>";
        this.help = "Allow or disallow sending links in message";
        this.aliases = Collections.singletonList("antilink");
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.AUTOMOD;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        boolean antilink;

        if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off"))
            antilink = false;
        else if (input.equalsIgnoreCase("on"))
            antilink = true;
        else {
            ctx.reply("Not a valid input");
            return;
        }

        DataSource.INS.antiLinks(ctx.getGuildId(), antilink);

        if (antilink)
            ctx.reply("Messages with not be filtered for links now");
        else
            ctx.reply("Messages with links will now be automatically deleted");

    }

}