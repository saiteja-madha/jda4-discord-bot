package bot.commands.admin.flag;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class FlagtrCommand extends ICommand {

    public FlagtrCommand() {
        this.name = "flagtr";
        this.help = "enable or disable translation by reaction";
        this.minArgsCount = 1;
        this.usage = "<ON | OFF>";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        String input = ctx.getArgs().get(0);

        if (input.equalsIgnoreCase("ON")) {
            DataSource.INS.setFlagTranslation(ctx.getGuild().getId(), true);
        } else if (input.equalsIgnoreCase("OFF")) {
            DataSource.INS.setFlagTranslation(ctx.getGuild().getId(), false);
        } else {
            ctx.reply("Incorrect usage. Please provide valid arguments");
            return;
        }

        ctx.reply("Success. Configuration saved!");

    }

}