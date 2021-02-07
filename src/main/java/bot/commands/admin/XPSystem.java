package bot.commands.admin;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class XPSystem extends ICommand {

    public XPSystem() {
        this.name = "xpsystem";
        this.help = "enable or disable XP ranking system in the server";
        this.usage = "<ON | OFF>";
        this.minArgsCount = 1;
        this.aliases = Arrays.asList("rankingsystem", "ranking");
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        boolean ranking;

        if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off"))
            ranking = false;
        else if (input.equalsIgnoreCase("on"))
            ranking = true;
        else {
            ctx.reply("Not a valid input");
            return;
        }

        DataSource.INS.xpSystem(ctx.getGuild().getId(), ranking);
        ctx.reply("Configuration saved! XP System is now " + (ranking ? "enabled" : "disabled"));

    }

}
