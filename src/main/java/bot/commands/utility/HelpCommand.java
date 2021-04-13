package bot.commands.utility;

import bot.Config;
import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HelpCommand extends ICommand {

    private final EventWaiter waiter;

    public HelpCommand(EventWaiter waiter) {
        this.name = "help";
        this.help = "Shows the list with commands in the bot";
        this.usage = "<command>";
        this.aliases = Arrays.asList("commands", "cmds", "commandlist");
        this.category = CommandCategory.UTILS;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.waiter = waiter;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            if (ctx.getSelfMember().hasPermission(ctx.getChannel(),
                    Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
                this.sendReactionHelpMenu(ctx);
            } else {
                this.sendCategoryHelpMenu(ctx);
            }
            return;
        }

        final String invoke = ctx.getArgs().get(0);
        CommandCategory category = CommandCategory.fromSearch(invoke);

        if (invoke.equalsIgnoreCase("info"))
            category = CommandCategory.INFORMATION;

        if (category == CommandCategory.ADMINISTRATION || category == CommandCategory.AUTOMOD) {
            if (!ctx.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                ctx.reply("You must have `Manager Server` permission to view commands in this category");
                return;
            }
        }

        if (category == CommandCategory.OWNER) {
            if (!Config.get("OWNER_ID").equalsIgnoreCase(ctx.getAuthor().getId())) {
                ctx.reply("Only bot owner can view commands in this category");
                return;
            }
        }

        if (category != null) {
            ctx.reply(this.getCategoryHelpEmbed(ctx, category).build());
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

    private void sendReactionHelpMenu(CommandContext ctx) {
        final CommandCategory[] values = CommandCategory.values();

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor("Help Menu")
                .setDescription("Hello I am " + ctx.getSelfMember().getUser().getName() + "!\n" +
                        "A cool multipurpose discord bot which can serve all your needs");

        for (CommandCategory value : values) {
            if (value == CommandCategory.UNLISTED || value == CommandCategory.OWNER)
                continue;
            embed.addField(value.getName(), "React with " + value.getEmote() + " to view `" + value.getName().toLowerCase() + "` commands", true);
        }

        embed.addField("_", "[**Support Server**](" + Config.get("DISCORD_INVITE") + ") **|** [**Invite Link**](" + Config.get("BOT_INVITE") + ")", false);
        ctx.getChannel().sendMessage(embed.build()).queue((sentMessage) -> {
            for (CommandCategory value : values) {
                if (value.getEmote() == null)
                    continue;
                sentMessage.addReaction(value.getEmote()).queue();
            }

            waitForReactions(ctx, sentMessage);

        });

    }

    private void sendCategoryHelpMenu(CommandContext ctx) {
        final User selfUser = ctx.getSelfMember().getUser();

        if (ctx.getArgs().isEmpty()) {
            String str = "**About Me:**\n";
            str += "Hello I am " + selfUser.getName() + "!\n";
            str += "A cool multipurpose discord bot which can serve all your needs\n\n";
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
            str += Constants.ARROW + " invite" + "\n\n";
            str += "**Admin Modules:**" + "\n";
            str += Constants.ARROW + " admin" + "\n";
            str += Constants.ARROW + " automod" + "\n";

            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setDescription(str)
                    .setThumbnail(selfUser.getEffectiveAvatarUrl())
                    .setColor(Constants.TRANSPARENT_EMBED)
                    .setFooter("Type " + ctx.getPrefix() + "help <module> to see all related command",
                            ctx.getAuthor().getEffectiveAvatarUrl());

            ctx.reply(embed.build());

        }

    }

    private EmbedBuilder getCategoryHelpEmbed(CommandContext ctx, CommandCategory category) {
        return this.noPaginationHelp(ctx, category);
    }

    private EmbedBuilder noPaginationHelp(CommandContext ctx, CommandCategory category) {
        String collector;
        if (category == CommandCategory.IMAGE) {
            collector = ctx.getCmdHandler().getCommands().stream()
                    .filter(cmd -> cmd.getCategory() == category)
                    .map(cmd -> "`" + cmd.getName() + "`")
                    .collect(Collectors.joining(", "));

            collector += "\n\n" +
                    "You can use these image commands in following formats\n" +
                    " **" + ctx.getPrefix() + "cmd:** Picks message authors avatar as image\n" +
                    " **" + ctx.getPrefix() + "cmd <@member>:** Picks mentioned members avatar as image\n" +
                    " **" + ctx.getPrefix() + "cmd <url>:** Picks image from provided URL\n" +
                    " **" + ctx.getPrefix() + "cmd [attachment]:** Picks attachment image";
        } else {
            collector = ctx.getCmdHandler().getCommands().stream()
                    .filter(cmd -> cmd.getCategory() == category)
                    .map(cmd -> Constants.ARROW + " `" + cmd.getName() + "` - " + cmd.getHelp())
                    .collect(Collectors.joining("\n"));
        }

        return EmbedUtils.getDefaultEmbed()
                .setAuthor(category.getName() + " Commands")
                .setThumbnail(category.getIconUrl())
                .setDescription(collector);

    }

    private void waitForReactions(CommandContext ctx, Message sentMessage) {
        final CommandCategory[] values = CommandCategory.values();
        this.wait(ctx, sentMessage, e -> {
            final String emoji = e.getReaction().getReactionEmote().getEmoji();
            for (CommandCategory value : values) {
                if (value.getEmote() != null && value.getEmote().equals(emoji)) {
                    sentMessage.editMessage(this.getCategoryHelpEmbed(ctx, value).build())
                            .queue((msg) -> msg.removeReaction(emoji, e.getUser())
                                    .queue((__) -> this.waitForReactions(ctx, sentMessage)));
                    break;
                }
            }
        });

    }

    private void wait(CommandContext ctx, Message sentMsg, Consumer<GuildMessageReactionAddEvent> action) {
        waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                e -> e.getUser().getId().equals(ctx.getAuthor().getId()) && e.getChannel().equals(ctx.getChannel()), action, 20, TimeUnit.SECONDS, new Timeout(sentMsg));
    }

    private static class Timeout implements Runnable {

        private final Message msg;
        private boolean ran = false;

        private Timeout(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            if (ran)
                return;
            ran = true;
            msg.clearReactions().queue();
        }

    }

}
