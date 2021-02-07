package bot.commands.moderation;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.PurgeType;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class PurgeAttachmentCommand extends ICommand {

    public PurgeAttachmentCommand() {
        this.name = "purgeattach";
        this.help = "deletes the specified amount of messages with attachments";
        this.usage = "<amount>";
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY};
        this.category = CommandCategory.MODERATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final int amount = ModerationUtils.checkPurgeAmount(ctx, 0);
        if (amount != 0)
            ModerationUtils.purge(ctx, PurgeType.ATTACHMENT, amount, null);
    }
}
