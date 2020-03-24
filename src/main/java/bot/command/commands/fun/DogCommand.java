package bot.command.commands.fun;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class DogCommand implements ICommand{

	@Override
	public void handle(CommandContext ctx) {
		final TextChannel channel = ctx.getChannel();
		

		WebUtils.ins.getJSONObject("https://dog.ceo/api/breeds/image/random").async((json) -> {
			String image = json.get("message").asText();
			
			final EmbedBuilder embed = EmbedUtils.embedImage(image);
			 channel.sendMessage(embed.build()).queue();
			
		});		
	}

	@Override
	public String getName() {
		return "dog";
	}

	@Override
	public String getHelp() {
        return "Shows a random dog image\n" +
        "```Usage: [prefix]dog```";
	}

}
