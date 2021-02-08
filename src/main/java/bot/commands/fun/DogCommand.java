package bot.commands.fun;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class DogCommand extends ICommand {

    public DogCommand() {
        this.name = "dog";
        this.help = "shows a random dog image";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.FUN;
        this.cooldown = 5;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        WebUtils.ins.getJSONObject("https://dog.ceo/api/breeds/image/random").async((json) -> {
            String image = json.get("message").asText();

            final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setImage(image)
                    .setFooter("Requested by " + ctx.getAuthor().getAsTag());

            ctx.reply(embed.build());

        }, err -> {
            LOGGER.error(err.getMessage());
            ctx.replyError(Constants.API_ERROR);
        });
    }

}
