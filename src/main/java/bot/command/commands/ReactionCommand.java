package bot.command.commands;

import java.util.List;

import bot.command.CommandContext;
import bot.command.ICommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ReactionCommand implements ICommand {

	@Override
	public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();	
		List<TextChannel> channels = message.getMentionedChannels();
		
		if (args.size() < 3 || channels.isEmpty()) {
			channel.sendMessage("Missing arguments").queue();
			return;
		}
		
		TextChannel tc = channels.get(0);
		String messageIdString = args.get(1);
		
		try {
			Long messageId = Long.parseLong(messageIdString);
			String emote = args.get(2);

			tc.addReactionById(messageId, emote).queue();			
		} catch (NumberFormatException e) {
			
		}				
	}

	@Override
	public String getName() {
		return "react";
	}

	@Override
	public String getHelp() {
        return "Reacts with an emoji to the mentioned message\n" +
                "```Usage: [prefix]react <#channel> <messageid> <emote>```";
	}

}
