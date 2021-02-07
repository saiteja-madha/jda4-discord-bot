package bot.commands.automod;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.utils.BotUtils;
import bot.utils.GuildUtils;
import com.jagrosh.jdautilities.commons.utils.TableBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class AutoModStatus extends ICommand {

    private final String[] headersList = new String[]{"Status"};
    private final String[] rowNames = new String[]{"Max Lines", "Max Mentions", "MaxRole Mentions", "AntiLinks", "AntiInvites"};

    public AutoModStatus() {
        this.name = "automodstatus";
        this.help = "check automod configuration for this guild";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.AUTOMOD;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final GuildSettings.Automod config = DataSource.INS.getSettings(ctx.getGuildId()).automod;

        if (config == null) {
            ctx.reply("No configuration found on your server");
            return;
        }

        final String[][] values = {
                {get(config.maxLines)},
                {get(config.maxMentions)},
                {get(config.maxRoleMentions)},
                {get(config.preventLinks)},
                {get(config.preventInvites)}
        };

        String table = new TableBuilder()
                .addHeaders(headersList)
                .addRowNames(rowNames)
                .setName("AutoMod")
                .frame(true)
                .setBorders(BotUtils.bordersToUse)
                .codeblock(true)
                .setValues(values)
                .build();

        TextChannel textChannelById = GuildUtils.getTextChannelById(ctx.getGuild(), config.logChannel);
        String logChannel = textChannelById == null ? "Not configured" : textChannelById.getAsMention();

        ctx.reply("LogChannel: " + logChannel + table);

    }

    private String get(int input) {
        return (input != 0) ? String.valueOf(input) : "NA";
    }

    private String get(boolean input) {
        return (input) ? "enabled" : "disabled";
    }

}
