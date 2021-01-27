package bot.commands.moderation;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KickCommand extends ICommand {

    public KickCommand() {
        this.name = "kick";
        this.help = "Kick a member off the server";
        this.usage = "<@member(s)> [reason]";
        this.minArgsCount = 2;
        this.userPermissions = new Permission[]{Permission.KICK_MEMBERS};
        this.botPermissions = new Permission[]{Permission.KICK_MEMBERS};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();
        List<Member> targetMembers = message.getMentionedMembers();

        if (targetMembers.isEmpty()) {
            ctx.reply("Please @mention the member(s) you want to kick!");
            return;
        }

        // Split content at last member mention
        String[] split = message.getContentRaw().split(targetMembers.get(targetMembers.size() - 1).getId() + "> ");
        final String reason = split[1];

        targetMembers
                .stream()
                // Filter out members with which bot and command author can interact
                .filter(target -> ModerationUtils.canInteract(ctx.getMember(), target, "kick", ctx.getChannel()))
                .forEach(member -> ModerationUtils.kick(message, member, reason));

    }

}
