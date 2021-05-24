package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.InviteType;
import bot.database.DataSource;
import bot.utils.BotUtils;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class InviteImportCommand extends ICommand {

    private final EventWaiter waiter;

    public InviteImportCommand(EventWaiter waiter) {
        this.name = "invitesimport";
        this.help = "add existing guild invites to users";
        this.aliases = Collections.singletonList("inviteimport");
        this.botPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.INVITE;
        this.waiter = waiter;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final Guild guild = ctx.getGuild();

        ctx.reply("Are you sure you want to continue? `Yes/No`");

        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> {
                    final Message message = e.getMessage();
                    final String reply = message.getContentRaw();

                    if (reply.equalsIgnoreCase("yes")) {
                        guild.retrieveInvites().queue((invites) -> {
                            for (Invite invite : invites) {
                                final User inviter = invite.getInviter();

                                if (inviter == null)
                                    continue;

                                int uses = invite.getUses();

                                if (uses == 0)
                                    continue;

                                DataSource.INS.incrementInvites(guild.getId(), inviter.getId(), uses, InviteType.ADDED);

                            }

                        }, err -> ctx.reply("Failed to retrieve invites! Try again later or contact support server"));

                        BotUtils.sendSuccessWithMessage(message, "Done! Previous invites added");

                    }

                }, 1, TimeUnit.MINUTES, () -> ctx.reply("Oops! You did not respond with a valid answer"));
    }

}