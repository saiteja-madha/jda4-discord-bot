package bot.command.commands.moderation;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.PurgeType;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class PurgeCommand extends ICommand {

    public PurgeCommand() {
        this.name = "purge";
        this.help = "deletes the specified amount of messages";
        this.usage = "<amount>";
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final int amount = ModerationUtils.checkPurgeAmount(ctx, 0);
        if (amount != 0)
            ModerationUtils.purge(ctx, PurgeType.ALL, amount, null);

    }
}
