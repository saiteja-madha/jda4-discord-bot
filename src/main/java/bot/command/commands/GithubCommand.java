package bot.command.commands;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GithubCommand extends ICommand {

    public GithubCommand() {
        this.name = "github";
        this.help = "Shows github statistics of a user";
        this.usage = "<username>";
        this.minArgsCount = 1;
    }

    private static boolean websiteProvided(String text) {
        if (text.startsWith("http://")) {
            return true;
        } else return text.startsWith("https://");
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            channel.sendMessage("You must provide a username to look up").queue();
            return;
        }

        WebUtils.ins.getJSONObject("https://api.github.com/users/" + args.get(0)).async((json) -> {
            if (!json.has("login")) {
                ctx.reply("No user found matching " + args.get(0));
                return;
            }

            final String username = json.get("login").asText();
            final String realName = !json.get("name").asText().equals("null") ? json.get("name").asText() : "Not provided.";
            final String githubId = json.get("id").asText();
            final String avatarUrl = json.get("avatar_url").asText();
            final String userPageLink = json.get("html_url").asText();
            final String followers = json.get("followers").asText();
            final String following = json.get("following").asText();
            final String bio = !json.get("bio").asText().equals("null") ? json.get("bio").asText() : "Not provided";
            final String location = !json.get("location").asText().equals("null") ? json.get("location").asText() : "Not provided";
            final String website = websiteProvided(json.get("blog").asText()) ? "[Click me](" + json.get("blog").asText() + ")" : "Not provided";

            EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setAuthor("GitHub User: " + username, userPageLink, avatarUrl)
                    .addField("User Info",
                            "**Real Name**: *" + realName + "*\n" +
                                    "**Location**: *" + location + "*\n" +
                                    "**GitHub ID**: *" + githubId + "*\n" +
                                    "**Website**: *" + website + "*\n", true)
                    .addField("Social Stats",
                            "**Followers**: *" + followers + "*\n" +
                                    "**Following**: *" + following + "*", true)
                    .setDescription("**Bio**:\n" + bio)
                    .setImage(avatarUrl)
                    .setColor(0x6e5494)
                    .setFooter("Requested by " + ctx.getAuthor().getAsTag());

            ctx.reply(embed.build());

        });

    }

}
