package bot.utils;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GuildUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(GuildUtils.class);

    // [totalCount, botCount, memberCount]
    public static void getMemberStats(@NotNull Guild guild, Consumer<long[]> callback) {
        getMembersList(guild, list -> {
            final long totalCount = list.size();
            final long botCount = list.stream().filter((m) -> m.getUser().isBot()).count();
            final long memCount = totalCount - botCount;
            callback.accept(new long[]{totalCount, botCount, memCount});
        });
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

    @Nullable
    public static Role getMutedRole(@NotNull Guild guild) {
        return guild.getRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase("Muted"))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static VoiceChannel getVoiceChannelById(Guild guild, String id) {
        if (id == null || id.equals(""))
            return null;
        return guild.getVoiceChannelById(id);
    }

    public static void setVoiceChannelName(VoiceChannel channel, String name) {
        try {
            channel.getManager().setName(name).queue((__) -> {
            }, (e) -> LOGGER.error("Set Voice Channel Failed: " + e.getMessage()));
        } catch (PermissionException | Error permEx) {
            LOGGER.error("Set Voice Channel Failed: " + permEx.getMessage());
        }
    }

}
