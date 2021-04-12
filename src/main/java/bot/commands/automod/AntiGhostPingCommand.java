package bot.commands.automod;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class AntiGhostPingCommand extends ICommand {

    public AntiGhostPingCommand() {
        this.name = "antighostping";
        this.minArgsCount = 1;
        this.usage = "<ON | OFF>";
        this.help = "Log deleted messages with member mentions (Requires automod-logging enabled)";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.AUTOMOD;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        boolean antighostping;

        if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off") || input.equalsIgnoreCase("disable"))
            antighostping = false;
        else if (input.equalsIgnoreCase("on"))
            antighostping = true;
        else {
            this.sendUsageEmbed(ctx, "Incorrect Argument");
            return;
        }

        DataSource.INS.antiGhostPing(ctx.getGuildId(), antighostping);

        if (!antighostping)
            ctx.replyWithSuccess("Anti-ghostping logging is now disabled");
        else
            ctx.replyWithSuccess("Anti-ghostping logging is now enabled");

    }

}