package bot.commands.automod;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.List;

public class AutoModLog extends ICommand {

    public AutoModLog() {
        this.name = "automodlog";
        this.usage = "<#channel | OFF>";
        this.help = "set log channel for all automod events";
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.AUTOMOD;
    }

    @Override
    public void handle(@Nonnull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        List<TextChannel> mentionedChannels = ctx.getMessage().getMentionedChannels();
        TextChannel targetChannel = null;

        if (!input.equalsIgnoreCase("OFF")) {
            if (mentionedChannels.isEmpty()) {
                ctx.reply("Please mention the channel where you want to log");
                return;
            }
            targetChannel = mentionedChannels.get(0);
            if (!ModerationUtils.canSendLogs(ctx.getChannel(), targetChannel))
                return;
            DataSource.INS.setAutomodLogChannel(ctx.getGuildId(), targetChannel.getId());
            ctx.replyWithSuccess("Configuration saved! Log-channel updated");
        }

    }

}
