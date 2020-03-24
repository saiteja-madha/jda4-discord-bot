package bot.command.commands.fun;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class CatCommand implements ICommand{

	@Override
	public void handle(CommandContext ctx) {
		final TextChannel channel = ctx.getChannel();
		

		WebUtils.ins.getJSONObject("https://aws.random.cat/meow").async((json) -> {
			String image = json.get("file").asText();
			
			final EmbedBuilder embed = EmbedUtils.embedImage(image);
			 channel.sendMessage(embed.build()).queue();
			
		});		
	}

	@Override
	public String getName() {
		return "cat";
	}

	@Override
	public String getHelp() {
        return "Shows a random cat image\n" +
        "```Usage: [prefix]cat```";
	}

}
