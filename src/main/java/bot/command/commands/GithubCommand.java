package bot.command.commands;

import java.util.List;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.entities.TextChannel;

public class GithubCommand implements ICommand {

	@Override
	public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();		
        
        if (args.isEmpty()) {
            channel.sendMessage("You must provide a username to look up").queue();
            return;
        }
        
        final String usn = args.get(0);
        
        WebUtils.ins.getJSONObject("https://apis.duncte123.me/insta/" + usn).async((json) -> {

        	if (json.get("message").asText() == "Not Found") {
                channel.sendMessage(json.get("error").get("message").asText()).queue();
                return;
            }

        });
        
	}

	@Override
	public String getName() {
		return "github";
	}

	@Override
	public String getHelp() {
        return "Shows github statistics of a user\n" +
                "```Usage: [prefix]github <username>```";
	}

}
