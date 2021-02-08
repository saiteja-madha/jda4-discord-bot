package bot.commands.admin;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class SetPrefixCommand extends ICommand {

    public SetPrefixCommand() {
        this.name = "setprefix";
        this.help = "Sets the prefix for this server";
        this.minArgsCount = 1;
        this.usage = "<new-prefix>";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String newPrefix = String.join("", ctx.getArgs());
        DataSource.INS.setPrefix(ctx.getGuild().getId(), newPrefix);

        ctx.replyWithSuccess(String.format("New prefix has been set to `%s`", newPrefix));

    }

}