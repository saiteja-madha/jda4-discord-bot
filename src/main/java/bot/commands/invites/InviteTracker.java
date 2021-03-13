package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class InviteTracker extends ICommand {

    public InviteTracker() {
        this.name = "invitetracker";
        this.help = "enable or disable invite tracking in the server\n" +
                "disabling this manually may cause greeting to work incorrectly";
        this.usage = "<ON | OFF>";
        this.minArgsCount = 1;
        this.aliases = Collections.singletonList("invite-tracker");
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        boolean inviteTracking;

        if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off"))
            inviteTracking = false;
        else if (input.equalsIgnoreCase("on"))
            inviteTracking = true;
        else {
            ctx.reply("Please provide a valid input");
            return;
        }

        DataSource.INS.inviteTracking(ctx.getGuild().getId(), inviteTracking);
        ctx.replyWithSuccess("Configuration saved! Invite Tracking is now " + (inviteTracking ? "enabled" : "disabled"));
    }

}
