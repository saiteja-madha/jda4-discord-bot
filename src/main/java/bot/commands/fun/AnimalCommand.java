package bot.commands.fun;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AnimalCommand extends ICommand {

    public AnimalCommand() {
        this.name = "animal";
        this.help = "Show a random image of selected animal type" +
                "\nAvailable names : llama, duck, alpaca, seal, camel, fox, lizard, bird, wolf, panda";
        this.usage = "<name>";
        this.minArgsCount = 1;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.FUN;
        this.cooldown = 5;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        String url = "https://apis.duncte123.me/animal/";
        String[] animals = {"llama", "duck", "alpaca", "seal", "camel", "fox", "lizard", "bird", "wolf", "panda"};

        if (!(Arrays.asList(animals).contains(args.get(0)))) {
            ctx.reply("NOT FOUND");
            return;
        }

        url = url + args.get(0);
        WebUtils.ins.getJSONObject(url).async((json) -> {
            if (!json.get("success").asBoolean()) {
                ctx.reply("Something went wrong, try again later");
                return;
            }

            final JsonNode data = json.get("data");
            final String image = data.get("file").asText();
            final EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setImage(image)
                    .setFooter("Requested by " + ctx.getAuthor().getAsTag());

            ctx.reply(embed.build());

        });

    }

}
