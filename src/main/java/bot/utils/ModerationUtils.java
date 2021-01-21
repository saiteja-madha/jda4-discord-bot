package bot.utils;

import bot.command.CommandContext;
import bot.data.PurgeType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ModerationUtils {

    private final static Pattern linkCheck = Pattern.compile("^(?:https?|ftp):\\/\\/[^\\s/$.?#].[^\\s]*$");

    public static boolean canInteract(Member mod, Member target, String action, TextChannel channel) {
        if (!mod.canInteract(target)) {
            BotUtils.sendMsg(channel, "Oops! You cannot `" + action + "` this member");
            return false;
        }

        final Member self = mod.getGuild().getSelfMember();

        if (!self.canInteract(target)) {
            BotUtils.sendMsg(channel, "Ugh! I cannot `" + action + "` this member, Are their roles above mine?");
            return false;
        }
        return true;
    }

    public static int checkPurgeAmount(@NotNull CommandContext ctx, int input) {
        final List<String> args = ctx.getArgs();
        int amount;

        String arg = args.get(input);

        try {
            amount = Integer.parseInt(arg);
        } catch (NumberFormatException ignored) {
            ctx.reply("`" + arg + "` is not a valid number");
            return 0;
        }

        if (amount < 2 || amount > 100) {
            ctx.reply("Amount must be at least 2 and at most 100");
            return 0;
        }
        return amount;
    }

    public static void purge(CommandContext ctx, PurgeType type, int amount, String argument) {
        final TextChannel channel = ctx.getChannel();
        List<Message> messageList = new ArrayList<>();

        for (Message message : channel.getIterableHistory()) {
            if (messageList.size() == amount)
                break;

            if (message.getTimeCreated().isBefore(OffsetDateTime.now().minusWeeks(2)))
                break;

            if (message.isPinned())
                continue;

            switch (type) {
                case ATTACHMENT:
                    if (!message.getAttachments().isEmpty()) {
                        messageList.add(message);
                    }
                    break;
                case BOT:
                    if (message.getAuthor().isBot()) {
                        messageList.add(message);
                    }
                    break;
                case LINK:
                    if (linkCheck.matcher(message.getContentRaw()).matches()) {
                        messageList.add(message);
                    }
                    break;
                case TOKEN:
                    if (message.getContentRaw().toLowerCase().contains(argument.toLowerCase())) {
                        messageList.add(message);
                    }
                    break;
                case USER:
                    if (message.getAuthor().getId().equals(argument)) {
                        messageList.add(message);
                    }
                    break;
                case ALL:
                    messageList.add(message);
                    break;
            }
        }

        final int size = messageList.size();

        if (size <= 1) {
            ctx.reply("Not found messages that can be purged!");
            return;
        }

        if (size > 100)
            messageList.remove(100);

        ctx.getMessage().delete().queue(null, e -> {/* Ignore */});
        ctx.getChannel().deleteMessages(messageList).queue((__) ->
                        BotUtils.sendMsg(channel, "Successfully purged `" + size + "` messages!", 7)
                , e -> {
                    ctx.reply("Purge Failed!");
                    System.out.println("Purge Failed: " + e.getMessage());
                });
    }

    public static void kick(Message msg, Member target, String reason) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        try {
            guild.kick(target, reason)
                    .reason(reason)
                    .queue((__) -> BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is kicked from this server!"),
                            (error) -> {
                                BotUtils.sendMsg(tc, "Could not kick " + target.getAsMention());
                                System.out.println("Kick Error - " + error.getMessage());
                            }
                    );
        } catch (Exception e) {
            System.out.println("Kick Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "Kick was not successful");
        }
    }

    public static void softBan(Message msg, Member target, String reason) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        try {
            guild.ban(target, 1, reason)
                    .reason(reason)
                    .queue((__) -> guild.unban(target.getUser()).reason("Softban").queue((___) ->
                                    BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is softbanned from this server!"),
                            (e) -> {
                                BotUtils.sendMsg(tc, "Could not softban " + target.getAsMention());
                                System.out.println("Softban Error - " + e.getMessage());
                            }), (e) -> {
                        BotUtils.sendMsg(tc, "Could not softban " + target.getAsMention());
                        System.out.println("Softban Error - " + e.getMessage());
                    });

        } catch (Exception e) {
            System.out.println("Softban Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "SoftBan was not successful");
        }

    }

    public static void ban(Message msg, Member target, String reason) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        try {
            guild.ban(target, 0, reason)
                    .reason(reason)
                    .queue((__) -> BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is banned from this server!"),
                            (error) -> {
                                BotUtils.sendMsg(tc, "Could not ban " + target.getAsMention());
                                System.out.println("Ban Error - " + error.getMessage());
                            }
                    );
            guild.kick(target, reason).reason(reason)
                    .queue((__) -> BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is kicked from this server!"),
                            (error) -> BotUtils.sendMsg(tc, "Could not kick " + target.getAsMention())
                    );
        } catch (Exception e) {
            System.out.println("Ban Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "Ban was not successful");
        }

    }

}
