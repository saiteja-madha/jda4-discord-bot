package bot.commands.automod;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MaxLinesCommand extends ICommand {

    public MaxLinesCommand() {
        this.name = "maxlines";
        this.minArgsCount = 1;
        this.usage = "<number | OFF>";
        this.help = "sets maximum lines allowed per message";
        this.aliases = Collections.singletonList("maxline");
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.AUTOMOD;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        int maxlines;

        try {
            maxlines = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off"))
                maxlines = 0;
            else {
                ctx.reply("Not a valid input");
                return;
            }
        }

        if (maxlines < 0) {
            ctx.reply("The maximum number of lines must be a positive integer!");
            return;
        }

        DataSource.INS.setMaxLines(ctx.getGuildId(), maxlines);

        if (maxlines == 0)
            ctx.reply("There is now no maximum line limit.");
        else
            ctx.reply("Messages longer than `" + maxlines + "` lines will now be automatically deleted");

    }

}