package bot.commands.invites;

import bot.Bot;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.handlers.InviteHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class InviteTracker extends ICommand {

    private final InviteHandler inviteHandler;

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
        this.inviteHandler = bot.getInviteHandler();
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final String input = ctx.getArgs().get(0);

        if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off")) {
            DataSource.INS.inviteTracking(guild.getId(), false);
            ctx.replyWithSuccess("Configuration saved! Invite Tracking is now disabled");
        } else if (input.equalsIgnoreCase("on")) {
            inviteHandler.enableTracking(guild);
            ctx.replyWithSuccess("Configuration saved! Invite Tracking is now enabled");
        } else {
            ctx.reply("Please provide a valid input");
        }

    }

}
