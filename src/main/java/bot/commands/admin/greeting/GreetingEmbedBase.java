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

public abstract class GreetingEmbedBase extends ICommand {

    private final GreetingType type;

    public GreetingEmbedBase(GreetingType type) {
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
                DataSource.INS.enableGreetingEmbed(ctx.getGuildId(), true, type);
                ctx.reply("Configuration saved!");
                break;

            case "off":
            case "disable":
                DataSource.INS.enableGreetingEmbed(ctx.getGuildId(), false, type);
                ctx.reply("Configuration saved!");
                break;

            case "desc":
                setupDescription(ctx);
                break;

            case "footer":
                setupFooter(ctx);
                break;

            case "color":
                setupColor(ctx);
                break;

            default:
                ctx.reply("Did you provide a valid argument?");

        }

    }

    private void setupDescription(CommandContext ctx) {
        if (ctx.getArgs().size() < 2) {
            ctx.reply("Insufficient Arguments! Please provide a embed description");
            return;
        }

        String description = String.join(" ", ctx.getArgs().subList(1, ctx.getArgs().size()));

        if (description.equalsIgnoreCase("off"))
            description = null;

        DataSource.INS.setGreetingDesc(ctx.getGuildId(), description, type);
        ctx.reply("Configuration saved!");

    }

    private void setupFooter(CommandContext ctx) {
        if (ctx.getArgs().size() < 2) {
            ctx.reply("Insufficient Arguments! Please provide a footer message");
            return;
        }

        String footer = String.join(" ", ctx.getArgs().subList(1, ctx.getArgs().size()));

        if (footer.equalsIgnoreCase("off"))
            footer = null;

        DataSource.INS.setGreetingFooter(ctx.getGuildId(), footer, type);
        ctx.reply("Configuration saved!");

    }

    private void setupColor(CommandContext ctx) {
        if (ctx.getArgs().size() < 2) {
            ctx.reply("Insufficient Arguments! Please provide a HEXColor input");
            return;
        }

        final String hex = ctx.getArgs().get(1);

        if (!MiscUtils.isHex(hex)) {
            ctx.reply("Err! Did you provide a valid image HEX Color code?");
            return;
        }

        DataSource.INS.setGreetingColor(ctx.getGuildId(), hex, type);
        ctx.reply("Configuration saved!");

    }

}
