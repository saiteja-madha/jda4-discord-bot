package bot.commands.utility;

import bot.Config;
import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HelpCommand extends ICommand {

    public HelpCommand() {
        this.name = "help";
        this.help = "Shows the list with commands in the bot";
        this.usage = "<command>";
        this.aliases = Arrays.asList("commands", "cmds", "commandlist");
        this.category = CommandCategory.UTILS;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final User selfUser = ctx.getSelfMember().getUser();

        if (ctx.getArgs().isEmpty()) {
            String str = "**About Me:**\n";
            str += "Hello I am " + selfUser.getName() + "!\n";
            str += "A cool multipurpose discord bot" + " which can serve all your needs\n\n";
            str += "**Quick Links:**" + "\n";
            str += "Support Server: [Join here](" + Config.get("DISCORD_INVITE") + ")" + "\n";
            str += "Invite Link: [Click me](" + Config.get("BOT_INVITE") + ")" + "\n\n";
            str += "**Command modules:**" + "\n";
            str += Constants.ARROW + " utility" + "\n";
            str += Constants.ARROW + " fun" + "\n";
            str += Constants.ARROW + " image" + "\n";
            str += Constants.ARROW + " information" + "\n";
            str += Constants.ARROW + " social" + "\n";
            str += Constants.ARROW + " moderation" + "\n";

            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setDescription(str)
                    .setThumbnail(selfUser.getEffectiveAvatarUrl())
                    .setColor(Constants.TRANSPARENT_EMBED)
                    .setFooter("Type " + ctx.getPrefix() + "help <module> to see all related command",
                            ctx.getAuthor().getEffectiveAvatarUrl());

            ctx.reply(embed.build());
            return;

        }

        final String invoke = ctx.getArgs().get(0);
        CommandCategory category = CommandCategory.fromSearch(invoke);

        if (invoke.equalsIgnoreCase("info"))
            category = CommandCategory.INFORMATION;

        if (category == CommandCategory.ADMINISTRATION || category == CommandCategory.AUTOMOD)
            category = null;

        if (category == CommandCategory.OWNER) {
            if (!Config.get("OWNER_ID").equalsIgnoreCase(ctx.getAuthor().getId())) {
                return;
            }
        }

        if (category != null) {
            noPaginationHelp(ctx, category);
            return;
        }

        final ICommand cmd = ctx.getCmdHandler().getCommand(invoke);

        if (cmd == null) {
            ctx.reply("Err! Did you provide a valid command category/command?\n" +
                    "Type `" + ctx.getPrefix() + "help` to see all available commands");
            return;
        }

        cmd.sendUsageEmbed(ctx.getChannel(), ctx.getPrefix(), invoke, "Help");

    }

    private void noPaginationHelp(CommandContext ctx, CommandCategory category) {
        String collect = ctx.getCmdHandler().getCommands().stream()
                .filter(cmd -> cmd.getCategory() == category)
                .map(cmd -> "`" + cmd.getName() + "`")
                .collect(Collectors.joining(", "));

        if (category == CommandCategory.IMAGE)
            collect += "\n\n" +
                    "You can use these image commands in following formats\n" +
                    " **" + ctx.getPrefix() + "cmd:** Picks message authors avatar as image\n" +
                    " **" + ctx.getPrefix() + "cmd <@member>:** Picks mentioned members avatar as image\n" +
                    " **" + ctx.getPrefix() + "cmd <url>:** Picks image from provided URL\n" +
                    " **" + ctx.getPrefix() + "cmd [attachment]:** Picks attachment image";

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor(category.getSearch() + " Commands", null, category.getIconUrl())
                .setDescription(collect);

        ctx.reply(embed.build());

    }

}
