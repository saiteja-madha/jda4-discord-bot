package bot.commands.admin.mod_config;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MaxWarningsCommand extends ICommand {

    public MaxWarningsCommand() {
        this.name = "maxwarnings";
        this.help = "set max warnings a user can receive";
        this.usage = "<amount>";
        this.aliases = Collections.singletonList("setmaxwarnings");
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        try {
            int input = Integer.parseInt(args.get(0));
            if (input < 0 || input > 10) {
                ctx.reply("Minimum number of warnings can be between 0 and 10");
                return;
            }

            DataSource.INS.setMaxWarnings(ctx.getGuildId(), input);
            ctx.reply("Successfully updated max warnings to `" + input + "`");

        } catch (NumberFormatException e) {
            ctx.reply("Incorrect argument `" + args.get(0) + "`. Please provide a valid number input");
        }

    }

}
