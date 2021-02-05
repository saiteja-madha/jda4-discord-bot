package bot.commands.admin.mod_config;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ModLogChannel extends ICommand {

    public ModLogChannel() {
        this.name = "modlog";
        this.usage = "`{p}modlog <ON | OFF>` : enable/disable moderation logging\n" +
                "`{p}modlog <#channel>` : remove the existing ticket configuration\n";
        this.help = "set log channel for all moderation events";
        this.aliases = Collections.singletonList("modlogchannel");
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void handle(@Nonnull CommandContext ctx) {
        final String input = ctx.getArgs().get(0);

        if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off") || input.equalsIgnoreCase("on")) {
            boolean enableModLogs = false;
            if (input.equalsIgnoreCase("on"))
                enableModLogs = true;
            DataSource.INS.enableModlogs(ctx.getGuildId(), enableModLogs);

        } else if (!ctx.getMessage().getMentionedChannels().isEmpty()) {
            final TextChannel targetChannel = ctx.getMessage().getMentionedChannels().get(0);

            if (!ModerationUtils.canSendLogs(ctx.getChannel(), targetChannel))
                return;

            DataSource.INS.setModLogChannel(ctx.getGuildId(), targetChannel.getId());

        } else {
            ctx.reply("Incorrect command usage");
        }

    }

}
