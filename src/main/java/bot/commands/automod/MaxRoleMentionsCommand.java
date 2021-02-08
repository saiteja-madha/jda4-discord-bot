package bot.commands.automod;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.handlers.AutoModHandler;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MaxRoleMentionsCommand extends ICommand {

    public MaxRoleMentionsCommand() {
        this.name = "maxrolementions";
        this.minArgsCount = 1;
        this.usage = "<number | OFF>";
        this.aliases = Collections.singletonList("maxrolemention");
        this.help = "sets maximum role mentions allowed per message";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.AUTOMOD;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        int maxrolementions;

        try {
            maxrolementions = Integer.parseInt(input);
            if (maxrolementions < AutoModHandler.ROLE_MENTION_MINIMUM) {
                ctx.reply("Maximum mentions must atleast be " + AutoModHandler.MENTION_MINIMUM);
                return;
            }
        } catch (NumberFormatException ex) {
            if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off"))
                maxrolementions = 0;
            else {
                ctx.reply("Not a valid input");
                return;
            }
        }

        DataSource.INS.setMaxRoleMentions(ctx.getGuildId(), maxrolementions);

        if (maxrolementions == 0)
            ctx.replyWithSuccess("Maximum user mentions limit is disabled.");
        else
            ctx.replyWithSuccess("Messages having more than `" + maxrolementions + "` role mentions will now be automatically deleted");

    }

}