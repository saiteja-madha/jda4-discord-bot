package bot.commands.admin.reaction_role;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddReactionRoleCommand extends ICommand {

    public AddReactionRoleCommand() {
        this.name = "addrr";
        this.help = "Reacts with an emoji to the mentioned message";
        this.usage = "<#channel> <messageid> <emote> <@role>";
        this.minArgsCount = 4;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();
        List<TextChannel> channels = message.getMentionedChannels();
        List<Role> roles = message.getMentionedRoles();

        if (channels.isEmpty() || roles.isEmpty()) {
            ctx.reply("Incorrect usage! Please mention the channel and role");
            return;
        }

        TextChannel tc = channels.get(0);
        Role role = roles.get(0);
        String messageIdString = args.get(1);

        try {
            long messageId = Long.parseLong(messageIdString);
            String emote = args.get(2);

            tc.addReactionById(messageId, emote).queue();
            DataSource.INS.addReactionRole(ctx.getGuild().getId(), tc.getId(), messageIdString, role.getId(), emote);
            ctx.reply("Successfully added reaction role!");

        } catch (NumberFormatException e) {
            ctx.reply("Did you provide a valid messageId?");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ctx.reply("Failed to react! Did you provide valid arguments?");
        }

    }

}
