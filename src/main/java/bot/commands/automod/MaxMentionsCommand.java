package bot.commands.automod;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.handlers.AutoModHandler;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MaxMentionsCommand extends ICommand {

    public MaxMentionsCommand() {
        this.name = "maxmentions";
        this.minArgsCount = 1;
        this.usage = "<number | OFF>";
        this.help = "sets maximum user mentions allowed per message";
        this.aliases = Collections.singletonList("maxmention");
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.AUTOMOD;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        int maxmentions;

        try {
            maxmentions = Integer.parseInt(input);
            if (maxmentions < AutoModHandler.MENTION_MINIMUM) {
                ctx.reply("Maximum mentions must atleast be " + AutoModHandler.MENTION_MINIMUM);
                return;
            }
        } catch (NumberFormatException ex) {
            if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off"))
                maxmentions = 0;
            else {
                ctx.reply("Not a valid input");
                return;
            }
        }

        DataSource.INS.setMaxMentions(ctx.getGuildId(), maxmentions);

        if (maxmentions == 0)
            ctx.replyWithSuccess("Maximum user mentions limit is disabled.");
        else
            ctx.replyWithSuccess(
                    "Messages having more than `" + maxmentions + "` user mentions will now be automatically deleted");

    }

}