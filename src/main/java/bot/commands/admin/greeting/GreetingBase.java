package bot.commands.admin.greeting;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.GreetingType;
import bot.database.DataSource;
import bot.utils.ImageUtils;
import bot.utils.MiscUtils;
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
                "`{p}{i} preview` : preview the configured " + text + "\n" +
                "`{p}{i} desc <text>` : setup " + text + " embed description\n" +
                "`{p}{i} footer <text>` : setup " + text + " embed footer\n" +
                "`{p}{i} thumbnail <ON | OFF>` : enable/disable " + text + " embed thumbnail\n" +
                "`{p}{i} image <url | OFF>` : enable/disable " + text + " embed image\n" +
                "`{p}{i} color <HexColor>` : setup " + text + " embed color\n\n" +
                "**Replacements**\n```" +
                "{server} - Server Name\n" +
                "{member} - Member Name\n" +
                "{count} - Server Member Count\n" +
                "```";
        this.help = "setup " + text + " message in your discord server";
        this.multilineHelp = true;
        this.minArgsCount = 1;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final String input = args.get(0).toLowerCase();

        switch (input) {

            case "preview":
                this.sendPreview(ctx);
                break;

            case "desc":
                this.setupDescription(ctx);
                break;

            case "footer":
                this.setupFooter(ctx);
                break;

            case "thumbnail":
                this.setupThumbnail(ctx);
                break;

            case "image":
                this.setupImage(ctx);
                break;

            case "color":
                this.setupColor(ctx);
                break;

            default: {
                List<TextChannel> mentionedChannels = ctx.getMessage().getMentionedChannels();
                TextChannel targetChannel = null;

                if (!input.equals("off") && !input.equals("disable")) {
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

        }

    }

    private void sendPreview(CommandContext ctx) {
        ImageUtils.sendGreeting(ctx.getGuild(), ctx.getAuthor(), type, ctx.getChannel());
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

    private void setupThumbnail(CommandContext ctx) {
        if (ctx.getArgs().size() < 2) {
            ctx.reply("Insufficient Arguments! Please provide a valid argument (`ON/OFF`)");
            return;
        }

        boolean input;

        if (ctx.getArgs().get(1).equalsIgnoreCase("on"))
            input = true;
        else if (ctx.getArgs().get(1).equalsIgnoreCase("off"))
            input = false;
        else {
            ctx.reply("Invalid input! Setup failed");
            return;
        }

        DataSource.INS.setGreetingThumbnail(ctx.getGuildId(), input, type);
        ctx.replyWithSuccess("Configuration saved! Embed thumbnail " + ((input) ? "enabled" : "disabled"));

    }

    private void setupImage(CommandContext ctx) {
        if (ctx.getArgs().size() < 2) {
            ctx.reply("Insufficient Arguments! Please provide a valid image URL");
            return;
        }

        String url = ctx.getArgs().get(1);

        if (url.equalsIgnoreCase("OFF"))
            url = null;

        else if (!MiscUtils.isImageUrl(url)) {
            ctx.reply("Oops! That doesn't look like a valid image URL");
            return;
        }

        DataSource.INS.setGreetingImage(ctx.getGuildId(), url, type);
        ctx.replyWithSuccess("Configuration saved! Image background " + ((url == null) ? "removed" : "changed"));

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
