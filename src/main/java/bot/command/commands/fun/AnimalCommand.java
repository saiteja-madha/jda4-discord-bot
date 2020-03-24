package bot.command.commands.fun;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class AnimalCommand implements ICommand {
	@Override
	public void handle(CommandContext ctx) {
		final TextChannel channel = ctx.getChannel();
		final List<String> args = ctx.getArgs();

		String url = "https://apis.duncte123.me/animal/";
		String[] animals = { "llama", "duck", "alpaca", "seal", "camel", "fox", "lizard", "bird", "wolf", "panda" };

		if (args.size() < 1) {
			channel.sendMessage("Missing arguments").queue();
			return;
		}

		if (!(Arrays.asList(animals).contains(args.get(0)))) {
			channel.sendMessage("NOT FOUND").queue();
			return;
		}

		url = url + args.get(0);
		WebUtils.ins.getJSONObject(url).async((json) -> {
			if (!json.get("success").asBoolean()) {
				channel.sendMessage("Something went wrong, try again later").queue();
				System.out.println(json);
				return;
			}

			final JsonNode data = json.get("data");
			final String image = data.get("file").asText();
			final EmbedBuilder embed = EmbedUtils.embedImage(image);

			channel.sendMessage(embed.build()).queue();
		});

	}

	@Override
	public String getName() {
		return "animal";
	}

	@Override
	public String getHelp() {
		return "Show a random image of selected animal type\nAvailable names : llama, duck, alpaca, seal, camel, fox, lizard, bird, wolf, panda" +
				"```Usage: [prefix]animal <name>```";
	}
}