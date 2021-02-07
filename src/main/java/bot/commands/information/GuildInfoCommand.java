package bot.commands.information;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.GuildUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuildInfoCommand extends ICommand {

    public GuildInfoCommand() {
        this.name = "serverinfo";
        this.help = "shows information about the discord server";
        this.aliases = Arrays.asList("guildinfo", "sinfo", "ginfo");
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.INFORMATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();

        GuildUtils.getMembersList(guild, (list) -> {
            GuildStats gs = new GuildStats(guild, list);

            String str = "";
            str = str + Constants.ARROW + " **Id:** " + gs.id + "\n";
            str = str + Constants.ARROW + " **Name:** " + gs.name + "\n";
            str = str + Constants.ARROW + " **Owner:** " + gs.owner + "\n";
            str = str + Constants.ARROW + " **Region:** " + gs.region + "\n";
            str = str + "\n";

            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setThumbnail(guild.getIconUrl())
                    .setTitle("GUILD INFORMATION")
                    .setDescription(str)
                    .addField("Server Members [" + gs.all + "]", "```Members: " + gs.users + "\nBots: "
                            + gs.bots + "```", true)
                    .addField("Online Stats [" + gs.onlineAll + "]", "```Members: " + gs.onlineUsers + "\nBots: "
                            + gs.onlineBots + "```", true)
                    .addField("Categories and channels [" + gs.totalChannels + "]",
                            "```Categories: " + gs.categories + " | Text: " + gs.textChannels + " | Voice: "
                                    + gs.voiceChannels + "```", false)
                    .addField("Normal Emotes", "```" + gs.emotesNormal + "```", true)
                    .addField("Animated Emotes", "```" + gs.emotesAnimated + "```", true)
                    .addField("Roles [" + gs.rolesCount + "]", "```" + gs.roles + "```", false)
                    .addField("Verification", "```" + gs.verificationLevel + "```", false)
                    .addField("AFK Channel", "```" + gs.afk + "```", true)
                    .addField("Boost Count", "```" + gs.boostCount + "```", true)
                    .addField("Server created on", "```" + gs.created + "```", false);

            if (guild.getSplashId() != null)
                embed.setImage(guild.getSplashUrl() + "?size=1024");

            ctx.reply(embed.build());

        });

    }

    private static class GuildStats {

        private final String name, id, region, afk, roles;
        private final int textChannels, voiceChannels, categories, rolesCount, totalChannels;
        private final long all, users, onlineUsers, bots, onlineBots, onlineAll;
        private final String owner;
        private final String created, emotesNormal, emotesAnimated, verificationLevel;
        private final int boostCount;

        private GuildStats(Guild g, List<Member> members) {
            this.name = g.getName();
            this.id = g.getId();
            this.region = g.getRegion().getName();
            this.created = g.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
            this.textChannels = g.getTextChannels().size();
            this.voiceChannels = g.getVoiceChannels().size();
            this.totalChannels = textChannels + voiceChannels;
            this.categories = g.getCategories().size();
            this.rolesCount = g.getRoles().size();
            this.afk = g.getAfkChannel() == null ? "Not set" : g.getAfkChannel().getName();
            this.owner = g.getOwner() != null ? g.getOwner().getUser().getAsTag() : "NA";
            this.all = members.size();
            this.users = members.stream().filter(m -> !m.getUser().isBot()).count();
            this.bots = all - users;
            this.onlineUsers = members.stream()
                    .filter(m -> !m.getUser().isBot() && !m.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count();
            this.onlineBots = members.stream()
                    .filter(m -> m.getUser().isBot() && !m.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count();
            this.onlineAll = this.onlineUsers + this.onlineBots;
            this.roles = g.getRoles().stream().filter(r -> !r.getName().contains("everyone"))
                    .map(r -> String.format("%s[%d]", r.getName(), getMembersInRole(members, r)))
                    .collect(Collectors.joining(", "));

            String[] emotesDetails = GuildUtils.getEmotesDetails(g);

            this.emotesNormal = emotesDetails[1];
            this.emotesAnimated = emotesDetails[2];
            this.boostCount = g.getBoostCount();

            switch (g.getVerificationLevel()) {
                case VERY_HIGH:
                    this.verificationLevel = "┻�?┻ミヽ(ಠ益ಠ)ノ彡┻�?┻";
                    break;
                case HIGH:
                    this.verificationLevel = "(╯°□°）╯︵ ┻�?┻";
                    break;
                default:
                    this.verificationLevel = g.getVerificationLevel().name();
                    break;
            }
        }

        long getMembersInRole(List<Member> members, Role role) {
            return members.stream().filter(m -> m.getRoles().contains(role)).count();
        }

    }

}
