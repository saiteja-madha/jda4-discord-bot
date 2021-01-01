package bot.command.commands.fun;

import bot.command.CommandContext;
import bot.command.ICommand;
import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class JokeCommand extends ICommand {

    public JokeCommand() {
        this.name = "joke";
        this.help = "Shows a random joke";
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        WebUtils.ins.getJSONObject("https://apis.duncte123.me/joke").async((json) -> {
            if (!json.get("success").asBoolean()) {
                ctx.reply("Something went wrong, try again later");
                return;
            }

            final JsonNode data = json.get("data");
            final String title = data.get("title").asText();
            final String url = data.get("url").asText();
            final String body = data.get("body").asText();

            final EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setTitle(title, url)
                    .setDescription(body);

            ctx.reply(embed.build());
        });
    }

}
