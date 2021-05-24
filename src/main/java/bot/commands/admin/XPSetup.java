package bot.commands.admin;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class XPSetup extends ICommand {

    public XPSetup() {
        this.name = "xpsetup";
        this.usage = "`{p}{i} <ON/OFF>` : start interactive ticket setup\n" +
                "`{p}{i} message <new-message>` : setup level up notification channel\n" +
                "`{p}{i} channel <#channel>` : setup level up notification channel\n" +
                "`{p}{i} addrank <@role> <xp>` : setup ranks for certain XP\n" +
                "`{p}{i} remrank <@role>`: setup ranks for certain XP\n";
        this.help = "setup xp system in your discord server";
        this.multilineHelp = true;
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);

        switch (input.toLowerCase()) {
            case "on":
                DataSource.INS.xpSystem(ctx.getGuild().getId(), true);
                ctx.replyWithSuccess("Configuration saved! XP System is now enabled");
                break;

            case "off":
                DataSource.INS.xpSystem(ctx.getGuild().getId(), false);
                ctx.replyWithSuccess("Configuration saved! XP System is now disabled");
                break;

            case "message":
                this.setupMessage(ctx);
                break;

            case "channel":
                this.setupChannel(ctx);
                break;

            case "addrank":
                this.addRanks(ctx);
                break;

            case "remrank":
                this.remRanks(ctx);
                break;

            default:
                ctx.reply("Invalid input");
                break;

        }

    }

    private void setupMessage(CommandContext ctx) {
        if (!this.argsCheck(ctx))
            return;
    }

    private void setupChannel(CommandContext ctx) {
        if (!this.argsCheck(ctx))
            return;
    }

    private void addRanks(CommandContext ctx) {
        if (!this.argsCheck(ctx))
            return;
    }

    private void remRanks(CommandContext ctx) {
        if (!this.argsCheck(ctx))
            return;
    }

    private boolean argsCheck(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        if (args.size() < 2) {
            ctx.reply("Incorrect usage! Did you provide valid number of arguments?");
            return false;
        }
        return true;
    }

}