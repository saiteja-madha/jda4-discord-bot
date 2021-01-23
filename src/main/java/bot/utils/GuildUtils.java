package bot.utils;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GuildUtils {

    // [totalCount, botCount, memberCount]
    public static void getMemberStats(@NotNull Guild guild, Consumer<long[]> callback) {
        if (guild.isLoaded()) {
            List<Member> list = guild.getMembers();
            final long totalCount = list.size();
            final long botCount = list.stream().filter((m) -> m.getUser().isBot()).count();
            final long memCount = totalCount - botCount;

            callback.accept(new long[]{totalCount, botCount, memCount});
        } else {
            guild.loadMembers().onSuccess((list) -> {
                final long totalCount = list.size();
                final long botCount = list.stream().filter((m) -> m.getUser().isBot()).count();
                final long memCount = totalCount - botCount;
                callback.accept(new long[]{totalCount, botCount, memCount});
            }).onError((e) -> callback.accept(null));
        }
    }

    public static void getMembersList(@NotNull Guild guild, Consumer<List<Member>> callback) {
        if (guild.isLoaded()) {
            callback.accept(guild.getMembers());
        } else {
            guild.loadMembers().onSuccess(callback).onError(err -> callback.accept(Collections.emptyList()));
        }
    }

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

    public static String[] getEmotesDetails(@NotNull Guild guild) {
        List<Emote> emotes = guild.getEmotes();
        StringBuilder names = new StringBuilder();
        int total = 0, animated = 0;
        for (Emote emote : emotes) {
            total++;
            if (emote.isAnimated())
                animated++;
            names.append(emote.getName()).append(", ");
        }

        return new String[]{names.toString(), Integer.toString(total - animated), Integer.toString(animated)};
    }

}
