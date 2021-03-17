package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;

public class InviteRanksCommand extends ICommand {

    private final Paginator.Builder pBuilder;

    public InviteRanksCommand(EventWaiter waiter) {
        this.name = "inviteranks";
        this.help = "check the configured invite ranks";
        this.category = CommandCategory.INVITES;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.pBuilder = new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(5)
                .setFinalAction(m -> {
                })
                .setEventWaiter(waiter);
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        pBuilder.clearItems();
        final Map<Integer, String> inviteRanks = DataSource.INS.getSettings(ctx.getGuildId()).inviteRanks;

        if (inviteRanks.isEmpty()) {
            ctx.reply("No invite ranks are setup in this server");
            return;
        }

        inviteRanks.forEach((k, v) -> {
            final Role roleById = ctx.getGuild().getRoleById(v);

            if (roleById != null) {
                pBuilder.addItems("\u276F" + " " + k + " invites: " + roleById.getName() + " [`" + roleById.getId() + "`]");
            }

        });

        Paginator p = pBuilder
                .addUsers(ctx.getAuthor())
                .setText("**Invite Ranks**")
                .setColor(Color.decode(String.valueOf(0x3883d9)))
                .build();

        p.paginate(ctx.getChannel(), 1);
    }

}
