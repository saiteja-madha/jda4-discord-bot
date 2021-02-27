package bot.commands.information;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ChannelInfoCommand extends ICommand {

    private final static String LINESTART = Constants.ARROW + " ";

    public ChannelInfoCommand() {
        this.name = "channelinfo";
        this.help = "shows mentioned channel information";
        this.aliases = Collections.singletonList("cinfo");
        this.usage = "[#channel]";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.INFORMATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        TextChannel targetChannel;

        if (ctx.getArgs().isEmpty()) {
            targetChannel = ctx.getChannel();
        } else {
            if (!ctx.getMessage().getMentionedChannels().isEmpty()) {
                targetChannel = ctx.getMessage().getMentionedChannels().get(0);
            } else {
                List<TextChannel> tcByName = FinderUtil.findTextChannels(ctx.getArgsJoined(), ctx.getGuild());
                if (tcByName.isEmpty()) {
                    ctx.reply("No channels found matching `" + ctx.getArgs().get(0) + "`!");
                    return;
                }
                if (tcByName.size() != 1) {
                    ctx.reply("Multiple channels found matching `" + ctx.getArgs().get(0) + "`.Please be more specific");
                    return;
                }
                targetChannel = tcByName.get(0);
            }
        }

        final String name = targetChannel.getName();
        final String id = targetChannel.getId();
        final String topic = targetChannel.getTopic() == null ? "No topic set" : targetChannel.getTopic();
        final String category = targetChannel.getParent() == null ? "NA" : targetChannel.getParent().getName();
        final int slowmode = targetChannel.getSlowmode();
        final String created = targetChannel.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final int position = targetChannel.getPosition();
        final String type = targetChannel.getType().name();

        StringBuilder description = new StringBuilder(""
                + LINESTART + "ID: **" + id + "**\n"
                + LINESTART + "Creation: **" + created + "**\n"
                + LINESTART + "Type: **" + type + "**\n"
                + LINESTART + "Name: **" + name + "**\n"
                + LINESTART + "Category: **" + category + "**\n"
                + LINESTART + "Topic: **" + topic + "**\n"
                + LINESTART + "Position: **" + position + "**\n"
                + LINESTART + "Slowmode: **" + slowmode + "**\n"
                + LINESTART + "isNSFW: **" + (targetChannel.isNSFW() ? Constants.TICK : Constants.X_MARK) + "**\n"
                + LINESTART + "isNews: **" + (targetChannel.isNews() ? Constants.TICK : Constants.X_MARK) + "**\n"
        );

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor("Channel Details")
                .setDescription(description);

        ctx.reply(embed.build());

    }

}
