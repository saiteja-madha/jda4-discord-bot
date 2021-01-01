package bot.command.commands.admin;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class SetPrefixCommand extends ICommand {

    public SetPrefixCommand() {
        this.name = "setprefix";
        this.help = "Sets the prefix for this server";
        this.usage = "<new-prefix>";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String newPrefix = String.join("", ctx.getArgs());
        DataSource.INS.setPrefix(ctx.getGuild().getIdLong(), newPrefix);

        ctx.reply(String.format("New prefix has been set to `%s`", newPrefix));

    }

}