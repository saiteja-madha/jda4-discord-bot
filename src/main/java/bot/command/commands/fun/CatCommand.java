package bot.command.commands.fun;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class CatCommand extends ICommand {

    public CatCommand() {
        this.name = "cat";
        this.help = "Shows a random cat image";
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        WebUtils.ins.getJSONObject("https://aws.random.cat/meow").async((json) -> {
            String image = json.get("file").asText();

            final EmbedBuilder embed = EmbedUtils.embedImage(image);
            ctx.reply(embed.build());

        });
    }

}
