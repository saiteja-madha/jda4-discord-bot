package bot.command.commands.fun;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class DogCommand extends ICommand {

    public DogCommand() {
        this.name = "dog";
        this.help = "Shows a random dog image";
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        WebUtils.ins.getJSONObject("https://dog.ceo/api/breeds/image/random").async((json) -> {
            String image = json.get("message").asText();

            final EmbedBuilder embed = EmbedUtils.embedImage(image);
            ctx.reply(embed.build());

        });
    }

}
