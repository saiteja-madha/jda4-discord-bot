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
        String text = type.getText().toLowerCase();
        this.usage = "`{p}{i} <ON | OFF>` : enable or disable " + text + " embed\n" +
                "`{p}{i} desc <text>` : setup " + text + " embed description\n" +
                "`{p}{i} footer <text>` : setup " + text + " embed footer\n" +
                "`{p}{i} color <HexColor>` : setup " + text + " embed color\n\n" +
                "**Replacements**\n```" +
                "{server} - Server Name\n" +
                "{member} - Member Name\n" +
                "{count} - Server Member Count\n" +
                "```";
        this.multilineHelp = true;
        this.help = "";
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
                ctx.replyWithSuccess("Configuration saved! " + type.getText() + " embed is now enabled");
                break;

            case "off":
            case "disable":
                DataSource.INS.enableGreetingEmbed(ctx.getGuildId(), false, type);
                ctx.replyWithSuccess("Configuration saved! " + type.getText() + " embed is now disabled");
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
                this.sendUsageEmbed(ctx, "Incorrect Arguments");

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
        ctx.replyWithSuccess("Configuration saved! Embed description updated");

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
        ctx.replyWithSuccess("Configuration saved! Embed footer updated");

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
        ctx.replyWithSuccess("Configuration saved! Embed color updated");

    }

}
