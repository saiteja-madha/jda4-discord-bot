package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddInvitesRankCommand extends ICommand {

    public AddInvitesRankCommand() {
        this.name = "addinviterank";
        this.help = "add auto-rank after reaching a particular number of invites";
        this.usage = "<@role> <invites>";
        this.minArgsCount = 2;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.INVITES;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        Role role = null;
        final List<Role> menRoles = ctx.getMessage().getMentionedRoles();

        if (!menRoles.isEmpty())
            role = menRoles.get(0);

        if (role == null) {
            String roleName = ctx.getArgs().get(0);
            final List<Role> roles = FinderUtil.findRoles(roleName, ctx.getGuild());
            if (!roles.isEmpty()) {
                role = roles.get(0);
            }
        }

        if (role == null) {
            ctx.reply("No roles found matching `" + ctx.getArgs().get(0) + "");
            return;
        }

        final String invites = ctx.getArgs().get(1);
        try {
            int inviteCount = Integer.parseInt(invites);
            DataSource.INS.addInvitesRank(ctx.getGuildId(), role.getId(), inviteCount);
            ctx.reply("Success! Configuration saved");
        } catch (NumberFormatException ex) {
            ctx.reply("Did you provide a valid number of invites?");
        }

    }

}
