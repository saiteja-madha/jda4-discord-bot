package bot.commands.information;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoCommand extends ICommand {

    public UserInfoCommand() {
        this.name = "userinfo";
        this.help = "shows information about the user";
        this.aliases = Arrays.asList("uinfo", "memberinfo");
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.INFORMATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        Member member = ctx.getMember();
        final List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();

        if (!mentionedMembers.isEmpty())
            member = mentionedMembers.get(0);

        final String NAME = member.getEffectiveName();
        final String TAG = member.getUser().getAsTag();

        final String GUILD_JOIN_DATE = member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String DISCORD_JOINED_DATE = member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String ID = member.getUser().getId();
        final String STATUS = member.getOnlineStatus().getKey();
        final String ROLES = member.getRoles().stream().map(Role::getName).collect(Collectors.joining(", "));
        final String AVATAR = member.getUser().getEffectiveAvatarUrl();

        String ACTIVITY = null;
        if (!member.getActivities().isEmpty()) {
            final Activity activity = member.getActivities().get(0);
            final Activity.ActivityType type = activity.getType();

            if (type == Activity.ActivityType.DEFAULT)
                ACTIVITY = "Playing " + activity.getName();
            if (type == Activity.ActivityType.STREAMING)
                ACTIVITY = "Streaming " + activity.getName();
            if (type == Activity.ActivityType.LISTENING)
                ACTIVITY = "Listening to " + activity.getName();
            if (type == Activity.ActivityType.WATCHING)
                ACTIVITY = "Watching " + activity.getName();
            if (type == Activity.ActivityType.CUSTOM_STATUS)
                ACTIVITY = activity.getName();

        }

        EmbedBuilder embed = EmbedUtils.defaultEmbed()
                .setThumbnail(AVATAR)
                .setDescription("**User information for " + member.getUser().getName() + ":**")
                .addField("Name / Nickname", NAME, true)
                .addField("User Tag", TAG, true)
                .addField("ID", ID, false);

        if (ctx.getJDA().getGatewayIntents().contains(GatewayIntent.GUILD_PRESENCES))
            embed.addField("Current Status", STATUS, true);

        if (ACTIVITY != null)
            embed.addField("Activity ", ACTIVITY, true);

        embed.addField("Roles", ROLES.isEmpty() ? "-" : ROLES, false)
                .addField("Guild Joined", GUILD_JOIN_DATE, false)
                .addField("Discord Joined", DISCORD_JOINED_DATE, false)
                .addField("Avatar-URL", AVATAR, false);

        if (!member.equals(ctx.getMember()))
            embed.setFooter("Requested By: " + ctx.getAuthor().getAsTag());

        ctx.reply(embed.build());

    }

}
