package bot.commands.admin.greeting;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.GreetingType;
import bot.database.DataSource;
import bot.utils.MiscUtils;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GreetingImageBase extends ICommand {

    private final GreetingType type;

    public GreetingImageBase(GreetingType type) {
        this.type = type;
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final String input = args.get(0);

        switch (input.toLowerCase()) {
            case "on":
            case "enable":
                DataSource.INS.enableGreetingImage(ctx.getGuildId(), true, type);
                ctx.reply("Configuration saved!");
                break;

            case "off":
            case "disable":
                DataSource.INS.enableGreetingImage(ctx.getGuildId(), false, type);
                ctx.reply("Configuration saved!");
                break;

            case "msg":
                setupMessage(ctx);
                break;

            case "bkg":
            case "background":
                setupBackground(ctx);
                break;

            default:
                ctx.reply("Did you provide a valid argument?");

        }

    }

    private void setupMessage(CommandContext ctx) {
        if (ctx.getArgs().size() < 3) {
            ctx.reply("Incorrect Arguments! Please provide a valid message");
            return;
        }

        String message = String.join(" ", ctx.getArgs().subList(1, ctx.getArgs().size()));
        DataSource.INS.setGreetingImageMsg(ctx.getGuildId(), message, type);
        ctx.reply("Configuration saved!");

    }

    private void setupBackground(CommandContext ctx) {
        if (ctx.getArgs().size() < 2) {
            ctx.reply("Insufficient Arguments! Please provide a valid image URL");
            return;
        }

        String url = ctx.getArgs().get(1);

        if (!MiscUtils.isURL(url)) {
            ctx.reply("Oops! That doesn't look like a valid image URL");
            return;
        }

        DataSource.INS.setGreetingImageBkg(ctx.getGuildId(), url, type);
        ctx.reply("Configuration saved!");

    }

}
