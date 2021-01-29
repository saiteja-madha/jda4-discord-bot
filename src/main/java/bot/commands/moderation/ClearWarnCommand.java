package bot.commands.moderation;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class ClearWarnCommand extends ICommand {

    public ClearWarnCommand() {
        this.name = "clearwarnings";
        this.help = "clears previous warnings received by a user";
        this.minArgsCount = 1;
        this.usage = "<@member>";
        this.userPermissions = new Permission[]{Permission.KICK_MEMBERS};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();

        if (message.getMentionedMembers().isEmpty()) {
            ctx.reply("Please @mention the user you want to clear warnings for!");
            return;
        }

        final Member target = message.getMentionedMembers().get(0);

        if (!ModerationUtils.canInteract(ctx.getMember(), target, "clean warnings of", ctx.getChannel())) {
            return;
        }

        DataSource.INS.deleteWarnings(target);
        ctx.reply("All warnings for " + target.getUser().getAsTag() + " are cleared!");

    }

}
