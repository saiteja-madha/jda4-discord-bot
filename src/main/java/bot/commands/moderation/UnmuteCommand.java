package bot.commands.moderation;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnmuteCommand extends ICommand {

    public UnmuteCommand() {
        this.name = "unmute";
        this.help = "unmutes the specified user";
        this.usage = "<@user>";
        this.minArgsCount = 1;
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
        this.userPermissions = new Permission[]{Permission.KICK_MEMBERS};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final Message message = ctx.getMessage();

        if (message.getMentionedMembers().isEmpty()) {
            ctx.reply("Please @mention the user you want to unmute!");
            return;
        }

        String reason = String.join(" ", args.subList(1, args.size()));

        final Member target = message.getMentionedMembers().get(0);

        if (!ModerationUtils.canInteract(ctx.getMember(), target, "unmute", ctx.getChannel())) {
            return;
        }

        ModerationUtils.unmute(message, target, reason);

    }

}
