package bot.commands.admin.greeting;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.GreetingType;
import bot.database.DataSource;
import bot.utils.ImageUtils;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GreetingBase extends ICommand {

    private final GreetingType type;

    public GreetingBase(GreetingType type) {
        this.type = type;
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final String input = args.get(0);

        switch (input.toLowerCase()) {

            case "on":
            case "enable":
                DataSource.INS.enableGreeting(ctx.getGuildId(), true, type);
                ctx.reply("Configuration saved!");
                break;

            case "off":
            case "disable":
                DataSource.INS.enableGreeting(ctx.getGuildId(), false, type);
                ctx.reply("Configuration saved!");
                break;

            case "channel":
                setChannel(ctx);
                break;

            case "preview":
                sendPreview(ctx);
                break;

            default:
                ctx.reply("Did you provide a valid argument?");

        }

    }

    private void setChannel(CommandContext ctx) {
        if (ctx.getMessage().getMentionedChannels().isEmpty()) {
            ctx.reply("Please mention the channel where you want to send the " + type.getText().toLowerCase() + " message");
            return;
        }

        DataSource.INS.setGreetingChannel(ctx.getGuildId(), ctx.getMessage().getMentionedChannels().get(0).getId(), type);
        ctx.reply("Configuration saved!");
    }

    private void sendPreview(CommandContext ctx) {
        ImageUtils.sendGreeting(ctx.getGuild(), ctx.getAuthor(), type, ctx.getChannel());
    }

}
