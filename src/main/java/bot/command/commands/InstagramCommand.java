package bot.command.commands;

import bot.command.CommandContext;
import bot.command.ICommand;
import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class InstagramCommand extends ICommand {

    public InstagramCommand() {
        this.name = "instagram";
        this.help = "Shows instagram statistics of a user with the latest image";
        this.usage = "<username>";
        this.aliases = Collections.singletonList("insta");
        this.argsCount = 1;
    }

    private String getLatestImage(JsonNode json) {
        if (!json.isArray() || json.size() == 0)
            return null;

        return json.get(0).get("url").asText();
    }

    private String toEmote(boolean bool) {
        return bool ? "✅" : "❌";
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final String usn = args.get(0);

        WebUtils.ins.getJSONObject("https://apis.duncte123.me/insta/" + usn).async((json) -> {
            if (!json.get("success").asBoolean()) {
                ctx.reply(json.get("error").get("message").asText());
                return;
            }

            final JsonNode user = json.get("user");
            final String username = user.get("username").asText();
            final String pfp = user.get("profile_pic_url").asText();
            final String biography = user.get("biography").asText();
            final boolean isPrivate = user.get("is_private").asBoolean();
            final int following = user.get("following").get("count").asInt();
            final int followers = user.get("followers").get("count").asInt();
            final int uploads = user.get("uploads").get("count").asInt();

            final EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setTitle("Instagram info of " + username, "https://www.instagram.com/" + username)
                    .setThumbnail(pfp)
                    .setDescription(String.format(
                            "**Private account:** %s\n**Bio:** %s\n**Following:** %s\n**Followers:** %s\n**Uploads:** %s",
                            toEmote(isPrivate),
                            biography,
                            following,
                            followers,
                            uploads
                    ))
                    .setImage(getLatestImage(json.get("images")));

            ctx.reply(embed.build());

        });
    }

}
