package bot.commands.utility;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class UrbanCommand extends ICommand {

    public UrbanCommand() {
        this.name = "urban";
        this.help = "searches the urban dictionary";
        this.usage = "<search-term>";
        this.minArgsCount = 1;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.UTILS;
        this.cooldown = 5;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String term = ctx.getArgsJoined();
        final String url = "http://api.urbandictionary.com/v0/define?term=" + term;

        WebUtils.ins.getJSONObject(url).async((json) -> {
            if (json.get("list").isEmpty()) {
                ctx.reply("Nothing found matching `" + term + "`");
                return;
            }

            final JsonNode item = json.get("list").get(0);
            final String permaLink = item.get("permalink").asText();

            final EmbedBuilder eb = EmbedUtils.defaultEmbed()
                    .setAuthor("Author: " + item.get("author").asText())
                    .setDescription("__**DEFINITION:**__\n\n")
                    .appendDescription(item.get("definition").asText())
                    .appendDescription("\n\n")
                    .addField("Example", item.get("example").asText(), false)
                    .addField("Upvotes: :thumbsup:", item.get("thumbs_up").asInt() + "", true)
                    .addField("Downvotes: :thumbsdown:", item.get("thumbs_down").asInt() + "", true)
                    .addField("Link:", "[" + permaLink + "](" + permaLink + ")", false);

            ctx.reply(eb.build());

        });

    }

}
