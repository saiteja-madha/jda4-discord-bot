package bot.utils;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class BotUtils {

    public static void sendMsg(TextChannel channel, String message) {
        channel.sendMessage(message).queue(null, (error) -> { /* Ignore */ });
    }

    public static void sendMsg(TextChannel channel, String message, int time) {
        channel.sendMessage(message)
                .queue((m) -> m.delete().queueAfter(time, TimeUnit.SECONDS, null, (error) -> { /* Ignore */ })
                        , null);
    }

    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        channel.sendMessage(embed).queue(null, (error) -> { /* Ignore */ });
    }

    public static void sendEmbed(TextChannel channel, MessageEmbed embed, int time) {
        channel.sendMessage(embed)
                .queue((m) -> m.delete().queueAfter(time, TimeUnit.SECONDS, null, (error) -> { /* Ignore */ })
                        , null);
    }

}
