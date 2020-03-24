package bot.command.commands.admin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.SQLiteDataSource;
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
			try (final PreparedStatement insertStatement = SQLiteDataSource
	                .getConnection()
	                .prepareStatement("INSERT INTO reaction_roles (guild_id, channel_id, message_id, emote, role_id) VALUES (?, ?, ?, ?, ?)")) {

				insertStatement.setLong(1, channel.getGuild().getIdLong());
				insertStatement.setLong(2, tc.getIdLong());
				insertStatement.setLong(3, messageId.longValue());
				insertStatement.setString(4, emote);
				insertStatement.setLong(5, role.getIdLong());
	            
				insertStatement.executeUpdate();
	            
	            try (final PreparedStatement updateStatement = SQLiteDataSource
		                .getConnection()
		                .prepareStatement("UPDATE guild_settings SET rr_enabled = 'Y' WHERE guild_id = ?")) {
		            
	            	updateStatement.setLong(1, channel.getGuild().getIdLong());
	            	updateStatement.executeUpdate();
	            	
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
	            channel.sendMessage("Successfully added reaction role!").queue();
	            
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
			
		} catch (NumberFormatException e) {
			
		}		
	}

	@Override
	public String getName() {
		return "addrr";
	}

	@Override
	public String getHelp() {
        return "Adds reaction role to the mentioned message\n" +
                "```Usage: [prefix]addrr <#channel> <messageid> <emote> <@role>```";
	}

}
