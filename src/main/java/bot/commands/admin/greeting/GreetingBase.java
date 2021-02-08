package bot.commands.admin.greeting;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.GreetingType;
import bot.database.DataSource;
import bot.utils.ImageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GreetingBase extends ICommand {

    private final GreetingType type;

    public GreetingBase(GreetingType type) {
        this.type = type;
        String text = type.getText().toLowerCase();
        this.usage = "`{p}{i} <#channel | OFF>` : enable or disable " + text + " message\n" +
                "`{p}{i} preview` : preview the configured " + text + "\n";
        this.help = "setup " + text + " message in your discord server";
        this.multilineHelp = true;
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final String input = args.get(0);

        if (input.equalsIgnoreCase("preview")) {
            sendPreview(ctx);
            return;
        }

        List<TextChannel> mentionedChannels = ctx.getMessage().getMentionedChannels();
        TextChannel targetChannel = null;

        if (!input.equalsIgnoreCase("off") && !input.equalsIgnoreCase("disable")) {
            if (mentionedChannels.isEmpty()) {
                this.sendUsageEmbed(ctx, "Incorrect Usage");
                return;
            }
            targetChannel = mentionedChannels.get(0);
        }

        DataSource.INS.setGreetingChannel(ctx.getGuildId(), (targetChannel == null) ? null : targetChannel.getId(), type);
        if (targetChannel == null)
            ctx.replyWithSuccess("Configuration saved! " + type.getText() + " message is disabled");
        else
            ctx.replyWithSuccess("Configuration saved! " + type.getText() + " message channel is set to " + targetChannel.getAsMention());


    }

    private void sendPreview(CommandContext ctx) {
        ImageUtils.sendGreeting(ctx.getGuild(), ctx.getAuthor(), type, ctx.getChannel());
    }

}
