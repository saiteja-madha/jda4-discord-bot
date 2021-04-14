package bot.commands.invites;

import bot.Bot;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.utils.BotUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class InviteTracker extends ICommand {

    private final Bot bot;

    public InviteTracker(Bot bot) {
        this.name = "invitetracker";
        this.help = "enable or disable invite tracking in the server\n" +
                "disabling this manually may cause greeting to work incorrectly";
        this.usage = "<ON | OFF>";
        this.minArgsCount = 1;
        this.aliases = Collections.singletonList("invite-tracker");
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.botPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.INVITE;
        this.bot = bot;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final Guild guild = ctx.getGuild();
        final String input = ctx.getArgs().get(0);

        if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off")) {
            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setDescription("Turning off invite tracking will delete all previous invite data from our system. " +
                            "This may also cause greeting messages to work incorrectly\n\n" +
                            "Are you sure you want to continue? `Yes/No`");

            ctx.reply(embed.build());

            bot.getWaiter().waitForEvent(GuildMessageReceivedEvent.class,
                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> {
                        final Message message = e.getMessage();
                        final String reply = message.getContentRaw();

                        if (reply.equalsIgnoreCase("yes")) {
                            DataSource.INS.clearInviteData(guild.getId());
                            DataSource.INS.inviteTracking(guild.getId(), false);

                            BotUtils.sendSuccessWithMessage(message, "Configuration saved! Invite Tracking is now disabled");

                        }
                    }, 1, TimeUnit.MINUTES, () -> ctx.reply("Oops! You did not respond with a valid answer"));

        } else if (input.equalsIgnoreCase("on")) {
            bot.getInviteHandler().enableTracking(guild);
            ctx.replyWithSuccess("Configuration saved! Invite Tracking is now enabled");

        } else {
            ctx.reply("Please provide a valid input");
        }

    }

}
