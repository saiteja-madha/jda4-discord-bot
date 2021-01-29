package bot.commands.moderation;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.database.objects.WarnLogs;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarningsCommand extends ICommand {

    private Paginator.Builder pBuilder;

    public WarningsCommand(EventWaiter waiter) {
        this.name = "warnings";
        this.help = "displays warnings received by mentioned user";
        this.minArgsCount = 1;
        this.usage = "<@user>";
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_HISTORY,
                Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MANAGE};
        this.pBuilder = new Paginator.Builder();
        pBuilder = new Paginator.Builder()
                .setItemsPerPage(3)
                .setFinalAction(m -> {
                })
                .setEventWaiter(waiter)
                .setTimeout(30, TimeUnit.SECONDS);
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();

        if (message.getMentionedMembers().isEmpty()) {
            ctx.reply("Please @mention the user you want to warn!");
            return;
        }

        final Member target = message.getMentionedMembers().get(0);

        List<WarnLogs> warnLogs = DataSource.INS.getWarnLogs(target);

        int page = 1;
        pBuilder.clearItems();

        if (!warnLogs.isEmpty()) {
            warnLogs.forEach((m) -> pBuilder.addItems("**ModName:** `" + m.modName + "`\n"
                    + "**Reason:** `" + (m.modReason == null || m.modReason.equals("") ? "Not Specified" : m.modReason) + "`\n"
                    + "**Timestamp:** `" + m.timeStamp + "`\n"));

            Paginator p = pBuilder.addUsers(ctx.getAuthor()).setText("Warnings received by `" + target.getUser().getAsTag() + "`")
                    .setColor(new Color(54, 57, 63)).build();

            p.paginate(ctx.getChannel(), page);

        } else
            ctx.reply("No warnings for `" + target.getUser().getAsTag() + "`");

    }

}
