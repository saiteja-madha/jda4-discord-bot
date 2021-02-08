package bot.commands.admin.mod_config;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ModLogChannel extends ICommand {

    public ModLogChannel() {
        this.name = "modlog";
        this.usage = "<#channel | OFF>";
        this.help = "enable/disable moderation logging";
        this.aliases = Collections.singletonList("modlogchannel");
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@Nonnull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);
        List<TextChannel> mentionedChannels = ctx.getMessage().getMentionedChannels();
        TextChannel targetChannel = null;

        if (!input.equalsIgnoreCase("off") && !input.equalsIgnoreCase("disable")) {
            if (mentionedChannels.isEmpty()) {
                ctx.reply("Please mention the channel where you want to log moderation events");
                return;
            }
            targetChannel = mentionedChannels.get(0);
            if (!ModerationUtils.canSendLogs(ctx.getChannel(), targetChannel))
                return;
        }

        DataSource.INS.setModLogChannel(ctx.getGuildId(), (targetChannel == null) ? null : targetChannel.getId());
        ctx.replyWithSuccess("Configuration saved! Log-channel updated");

    }

}
