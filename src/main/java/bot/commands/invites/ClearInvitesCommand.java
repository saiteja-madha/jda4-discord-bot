package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClearInvitesCommand extends ICommand {

    public ClearInvitesCommand() {
        this.name = "clearinvites";
        this.help = "clear a users added invites";
        this.usage = "<@member>";
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.INVITE;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();
        if (mentionedMembers.isEmpty()) {
            ctx.reply("Incorrect usage! You need to mention a member");
            return;
        }

        final Member target = mentionedMembers.get(0);
        DataSource.INS.clearInvites(ctx.getGuildId(), target.getId());
        ctx.replyWithSuccess("Configuration saved! Invites cleared for " + target.getEffectiveName());

    }

}
