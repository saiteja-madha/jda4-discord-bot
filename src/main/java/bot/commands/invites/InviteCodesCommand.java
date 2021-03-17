package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InviteCodesCommand extends ICommand {

    private final Paginator.Builder pBuilder;

    public InviteCodesCommand(EventWaiter waiter) {
        this.name = "invitecodes";
        this.help = "list all your invites codes in this guild";
        this.aliases = Collections.singletonList("invite-codes");
        this.botPermissions = new Permission[]{Permission.MANAGE_SERVER, Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.INVITES;
        this.pBuilder = new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(5)
                .setFinalAction(m -> {
                })
                .setEventWaiter(waiter);
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<Member> menMembers = ctx.getMessage().getMentionedMembers();
        Member target = ctx.getMember();

        if (!menMembers.isEmpty()) {
            target = menMembers.get(0);
        }

        final Member finalTarget = target;
        ctx.getGuild().retrieveInvites().queue(invites -> {
            final List<Invite> filter = invites.stream()
                    .filter(invite -> (invite.getInviter() != null
                            && invite.getInviter().getId().equalsIgnoreCase(finalTarget.getId()))
                    )
                    .collect(Collectors.toList());

            if (filter.isEmpty()) {
                ctx.reply("You do not have invite codes in this server");
                return;
            }

            pBuilder.clearItems();
            filter.forEach((invite) ->
                    pBuilder.addItems("\u276F" + " [" + invite.getCode() + "](" + invite.getUrl() + ") : " + invite.getUses() + " uses")
            );

            Paginator p = pBuilder
                    .addUsers(ctx.getAuthor())
                    .setText("Invite Codes for **" + finalTarget.getEffectiveName() + "**")
                    .setColor(Color.decode(String.valueOf(0x3883d9)))
                    .build();

            p.paginate(ctx.getChannel(), 1);

        });
    }

}
