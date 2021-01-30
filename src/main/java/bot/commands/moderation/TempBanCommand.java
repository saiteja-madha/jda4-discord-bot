package bot.commands.moderation;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.MiscUtils;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;
import java.util.List;

public class TempBanCommand extends ICommand {

    public TempBanCommand() {
        this.name = "tempban";
        this.help = "bans the mentioned member(s) for the specified amount of time";
        this.usage = "<@member(s)> <time> [reason]";
        this.minArgsCount = 2;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.botPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    public void handle(@Nonnull CommandContext ctx) {
        final Message message = ctx.getMessage();
        List<Member> targetMembers = message.getMentionedMembers();

        if (targetMembers.isEmpty()) {
            ctx.reply("Please @mention the member(s) you want to ban!");
            return;
        }

        // Split content at last member mention
        final String[] split = message.getContentRaw().split(targetMembers.get(targetMembers.size() - 1).getId() + ">");
        String content = split.length > 1 ? String.join(" ", split[1].split("\\s+")).trim() : null;

        if (content == null) {
            ctx.reply("Incorrect usage! Tempban time is not provided");
            return;
        }

        String[] data = content.split(" ", 2);
        int time = MiscUtils.parseTime(data[0]);
        String reason = data.length > 1 ? data[1] : "No reason provided";

        if (time == 0) {
            ctx.reply("Oops! Did you provide a valid time?\n" +
                    "Here are few valid time formats: `5m` `1h` `2d`");
            return;
        }

        targetMembers
                .stream()
                // Filter out members with which bot and command author can interact
                .filter(target -> ModerationUtils.canInteract(ctx.getMember(), target, "unmute", ctx.getChannel()))
                .forEach(member -> ModerationUtils.tempBan(message, member, reason, time));

    }
}
