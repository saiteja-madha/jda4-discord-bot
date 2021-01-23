package bot.command.commands.information;

import bot.Constants;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.MiscUtils;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class RoleInfoCommand extends ICommand {

    private final static String LINESTART = Constants.ARROW + " ";
    private final static String ROLE_EMOJI = "\uD83C\uDFAD"; // 🎭

    public RoleInfoCommand() {
        this.name = "roleinfo";
        this.help = "shows information of the specified role [No need to @mention]";
        this.usage = "<rolename>";
        this.aliases = Collections.singletonList("rinfo");
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        Role role;
        List<Role> rolesByMention = (ctx.getMessage().getMentionedRoles());

        if (rolesByMention.isEmpty()) {
            List<Role> rolesByName = FinderUtil.findRoles(ctx.getArgsJoined(), ctx.getGuild());
            if (rolesByName.isEmpty()) {
                ctx.reply("No Matching roles found!");
                return;
            }
            role = rolesByName.get(0);
        } else
            role = rolesByMention.get(0);

        final String title = (ROLE_EMOJI + " Roleinfo: " + MiscUtils.escapeMentions(role.getName()) + ":");
        Color color = role.getColor();

        StringBuilder description = new StringBuilder(""
                + LINESTART + "ID: **" + role.getId() + "**\n"
                + LINESTART + "Creation: **" + role.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "**\n"
                + LINESTART + "Position: **" + role.getPosition() + "**\n"
                + LINESTART + "Color: **#" + (color == null ? "000000" : Integer.toHexString(color.getRGB()).toUpperCase().substring(2)) + "**\n"
                + LINESTART + "Mentionable: **" + role.isMentionable() + "**\n"
                + LINESTART + "Hoisted: **" + role.isHoisted() + "**\n"
                + LINESTART + "Managed: **" + role.isManaged() + "**\n"
                + LINESTART + "Public Role: **" + (role.isPublicRole() ? Constants.TICK : Constants.X_MARK) + "**\n"
                + LINESTART + "Permissions: \n"
        );

        if (role.getPermissions().isEmpty())
            description.append("None");
        else
            description.append(role.getPermissions().stream().map(p -> "`, `" + p.getName()).reduce("", String::concat)
                    .substring(3)).append("`");

        EmbedBuilder eb = EmbedUtils.defaultEmbed()
                .setColor(color)
                .setDescription(description.toString())
                .setTitle(title);

        ctx.reply(eb.build());

    }
}
