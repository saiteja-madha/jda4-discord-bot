package bot.utils;

import net.dv8tion.jda.api.entities.Message;

public class GuildUtils {

    public static String resolveMentions(Message msg, String input) {

        for (int i = 0; i < msg.getMentionedUsers().size(); i++) {
            String replacement = msg.getMentionedUsers().get(i).getName();
            input = input.replaceFirst("<@!?[0-9]{17,19}>", replacement);
        }

        for (int i = 0; i < msg.getMentionedChannels().size(); i++) {
            String replacement = msg.getMentionedChannels().get(i).getName();
            input = input.replaceFirst("<#!?[0-9]{17,19}>", replacement);
        }

        for (int i = 0; i < msg.getMentionedRoles().size(); i++) {
            String replacement = msg.getMentionedRoles().get(i).getName();
            input = input.replaceFirst("<@&!?[0-9]{17,19}>", replacement);
        }

        return input;
    }

}
