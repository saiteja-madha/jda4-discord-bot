package bot.command.commands.admin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.SQLiteDataSource;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class RemoveReactionRoleCommand implements ICommand {

	@Override
	public void handle(CommandContext ctx) {
		
        final TextChannel channel = ctx.getChannel();
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();	
		List<TextChannel> channels = message.getMentionedChannels();
		
		if (args.size() < 2 || channels.isEmpty()) {
			channel.sendMessage("Missing arguments").queue();
			return;
		}
		
		TextChannel tc = channels.get(0);
		String messageIdString = args.get(1);
		
		try {
			Long messageId = Long.parseLong(messageIdString);			
			ctx.getMessage().clearReactions().queue();
			
			try (final PreparedStatement insertStatement = SQLiteDataSource
	                .getConnection()
	                .prepareStatement("DELETE FROM reaction_roles WHERE guild_id = ? AND channel_id = ? AND message_id = ?")) {

				insertStatement.setLong(1, channel.getGuild().getIdLong());
				insertStatement.setLong(2, tc.getIdLong());
				insertStatement.setLong(3, messageId.longValue());
	            
				insertStatement.executeUpdate();
	            
	            try (final PreparedStatement updateStatement = SQLiteDataSource
		                .getConnection()
		                .prepareStatement("UPDATE guild_settings SET rr_enabled = 'Y' WHERE guild_id = ?")) {
		            
	            	updateStatement.setLong(1, channel.getGuild().getIdLong());
	            	updateStatement.executeUpdate();
	            	
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
	            channel.sendMessage("Removed reaction role!").queue();
	            
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
			
		} catch (NumberFormatException e) {
			
		}		
	}

	@Override
	public String getName() {
		return "removerr";
	}

	@Override
	public String getHelp() {
        return "Remove reaction role to the mentioned message\n" +
                "```Usage: [prefix]removerr <#channel> <messageid> <emote> <@role>```";
	}

}
