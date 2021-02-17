package bot.utils;

import bot.Constants;
import com.jagrosh.jdautilities.commons.utils.TableBuilder.Borders;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BotUtils {

    public static final Borders bordersToUse = Borders.newHeaderRowNamesFrameBorders("\u2500", "\u2502", "\u253C",
            "\u251C", "\u2524", "\u252C", "\u2534", "\u250C", "\u2510", "\u2514", "\u2518", "\u2550", "\u256A",
            "\u255E", "\u2561", "\u256B", "\u256B", "\u2565", "\u2568", "\u256C", "\u2500", "\u2502");

    public static void sendMsg(TextChannel channel, String message) {
        if (channel != null) {
            if (channel.canTalk()) {
                channel.sendMessage(message).queue(null, error -> { /* Ignore */ });
            }
        }
    }

    public static void sendMsg(TextChannel channel, String message, int time) {
        if (channel != null) {
            if (channel.canTalk()) {
                channel.sendMessage(message)
                        .queue((m) -> m.delete().queueAfter(time, TimeUnit.SECONDS, null, (error) -> { /* Ignore */ })
                                , null);
            }
        }
    }

    public static void sendEmbed(TextChannel channel, EmbedBuilder embed) {
        sendEmbed(channel, embed.build());
    }

    public static void sendErrorEmbed(TextChannel channel, String message) {
        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setDescription(message)
                .setColor(Constants.ERROR_EMBED);

        sendEmbed(channel, embed);
    }

    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if (channel != null) {
            if (channel.canTalk()) {
                Guild guild = channel.getGuild();
                if (guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                    channel.sendMessage(embed).queue(null, (error) -> { /* Ignore */ });
                } else if (embed.getFields().isEmpty()) {
                    sendMsg(channel, embed.getDescription());
                }
            }
        }
    }

    public static void sendEmbed(TextChannel channel, MessageEmbed embed, int time) {
        if (channel != null) {
            if (channel.canTalk()) {
                Guild guild = channel.getGuild();
                if (guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                    channel.sendMessage(embed)
                            .queue((m) -> m.delete().queueAfter(time, TimeUnit.SECONDS, null, (error) -> { /* Ignore */ })
                                    , null);
                } else if (embed.getFields().isEmpty()) {
                    sendMsg(channel, embed.getDescription(), time);
                }
            }
        }
    }

    public static void sendDM(User user, EmbedBuilder embed, Consumer<? super Object> success, Consumer<? super Throwable> error) {
        if (user == null)
            return;

        user.openPrivateChannel()
                .flatMap((channel) -> channel.sendMessage(embed.build()))
                .queue(success, error);

    }

    public static void sendDM(User user, String message) {
        if (user == null)
            return;

        user.openPrivateChannel()
                .flatMap((channel) -> channel.sendMessage(message))
                .queue(null, (error) -> { /* Ignore */ });

    }

    public static void sendDM(User user, MessageEmbed embed) {
        if (user == null)
            return;

        user.openPrivateChannel()
                .flatMap((channel) -> channel.sendMessage(embed))
                .queue(null, (error) -> { /* Ignore */ });

    }

    public static void sendSuccess(Message message) {
        MessageUtils.sendSuccess(message);
    }

    public static void sendSuccessWithMessage(Message message, String content) {
        MessageUtils.sendSuccessWithMessage(message, content);
    }

    public static void sendErrorWithMessage(Message message, String content) {
        MessageUtils.sendErrorWithMessage(message, content);
    }

    public static String getEmbedHyperLink(String text, String link) {
        return "[" + text + "](" + link + ")";
    }

}
