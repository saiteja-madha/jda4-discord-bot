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
import java.util.Map;

public class RemInvitesRankCommand extends ICommand {

    public RemInvitesRankCommand() {
        this.name = "reminviterank";
        this.help = "remove invite rank configured with that role";
        this.usage = "<@role>";
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.INVITES;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Map<Integer, String> inviteRanks = DataSource.INS.getSettings(ctx.getGuildId()).inviteRanks;
        final List<Role> mentionedRoles = ctx.getMessage().getMentionedRoles();

        Role role = null;
        if (!mentionedRoles.isEmpty())
            role = mentionedRoles.get(0);
        else {
            final String searchString = ctx.getArgs().get(0);
            final List<Role> rolesByName = FinderUtil.findRoles(searchString, ctx.getGuild());
            if (!rolesByName.isEmpty())
                role = rolesByName.get(0);
        }

        if (role == null) {
            ctx.reply("Incorrect value. Please enter a role/roleId");
            return;
        }

        int invites = -1;
        for (Map.Entry<Integer, String> entry : inviteRanks.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();

            if (value.equalsIgnoreCase(role.getId())) {
                invites = key;
                break;
            }

        }

        if (invites == -1) {
            ctx.reply("No invite invites has been configured for this role: " + role.getName());
            return;
        }

        DataSource.INS.removeInvitesRank(ctx.getGuildId(), invites);
        ctx.reply("Configuration saved! Invite ranks has been removed");

    }

}