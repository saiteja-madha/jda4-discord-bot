package bot.command.commands.admin;

import java.util.List;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class AddReactionRoleCommand implements ICommand {

	@Override
	public void handle(CommandContext ctx) {
		
        final TextChannel channel = ctx.getChannel();
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();	
		List<TextChannel> channels = message.getMentionedChannels();
		List<Role> roles = message.getMentionedRoles();
		
		if (args.size() < 4 || roles.isEmpty() || channels.isEmpty()) {
			channel.sendMessage("Missing arguments").queue();
			return;
		}
		
		TextChannel tc = channels.get(0);
		Role role = roles.get(0);		
		String messageIdString = args.get(1);
		
		try {
			Long messageId = Long.parseLong(messageIdString);
			String emote = args.get(2);
			
			tc.addReactionById(messageId, emote).queue();
			DataSource.INS.addReactionRole(channel.getGuild().getIdLong(),channel.getIdLong(), messageId, role.getIdLong(), emote);
			channel.sendMessage("Successfully added reaction role!").queue();
			
		} catch (NumberFormatException e) {
			
		}		
	}

	@Override
	public String getName() {
		return "addrr";
	}

	@Override
	public String getHelp() {
        return "Reacts with an emoji to the mentioned message\n" +
                "```Usage: [prefix]addrr <#channel> <messageid> <emote> <@role>```";
	}

}
