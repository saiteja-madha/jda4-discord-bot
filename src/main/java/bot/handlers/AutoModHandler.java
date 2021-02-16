package bot.handlers;

import bot.Constants;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.utils.BotUtils;
import bot.utils.GuildUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoModHandler {

    public final static int MENTION_MINIMUM = 2;
    public final static int ROLE_MENTION_MINIMUM = 2;
    private final Pattern LINKS = Pattern.compile("https?:\\/\\/\\S+", Pattern.CASE_INSENSITIVE);

    public void performAutomod(GuildSettings.Automod config, Message message) {
        // Return if auto-moderation is not configured
        if (config == null)
            return;

        final Guild guild = message.getGuild();
        final TextChannel channel = message.getTextChannel();
        final String contentRaw = message.getContentRaw();

        // Check if the message can be auto-moderated
        if (!shouldModerate(Objects.requireNonNull(message.getMember()), channel))
            return;

        final TextChannel logChannel = GuildUtils.getTextChannelById(guild, config.logChannel);

        StringBuilder str = new StringBuilder();
        str.append(Constants.ARROW + " ").append("Author: `").append(message.getAuthor().getAsTag()).append("`\n");
        str.append(Constants.ARROW + " ").append("Channel: ").append(channel.getAsMention()).append("\n\n");
        str.append("**Reason:**").append("\n");
        boolean shouldDelete = false;

        // anti-mention (users)
        if (config.maxMentions >= MENTION_MINIMUM) {
            long mentions = message.getMentionedUsers().stream()
                    .filter(u -> !u.isBot() && !u.equals(message.getAuthor())).distinct().count();
            if (mentions > config.maxMentions) {
                shouldDelete = true;
                str.append("Mentions: ").append(mentions).append("\n");
            }
        }

        // anti-mention (roles)
        if (config.maxRoleMentions >= ROLE_MENTION_MINIMUM) {
            long mentions = message.getMentionedRoles().stream().distinct().count();
            if (mentions > config.maxRoleMentions) {
                str.append("RoleMentions: ").append(mentions).append("\n");
                shouldDelete = true;
            }
        }

        // max-newlines
        if (config.maxLines > 0) {
            int count = contentRaw.split("\n").length;
            if (count > config.maxLines) {
                str.append("NewLines: ").append(count).append("\n");
                shouldDelete = true;
            }
        }

        // anti-links
        if (!config.preventLinks) {
            // anti-discord invites

            if (config.preventInvites) {
                List<String> invites = message.getInvites();

                if (!invites.isEmpty()) {
                    shouldDelete = true;
                    str.append("Discord Invites: ").append(invites.size()).append("\n");
                }
            }

        } else {
            Matcher m = LINKS.matcher(contentRaw);
            if (m.matches()) {
                str.append("Links Found: ").append(Constants.TICK).append("\n");
                shouldDelete = true;
            }
        }

        // delete message if applicable
        if (shouldDelete) {
            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setAuthor("Auto Moderation")
                    .setDescription(str.toString())
                    .setFooter(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            try {
                message.delete().reason("Automod").queue(
                        (__) -> {
                            BotUtils.sendMsg(channel, Constants.TICK + " Auto-Moderation. Message deleted!", 3);
                            if (logChannel != null)
                                BotUtils.sendEmbed(logChannel, embed.build());
                            else
                                DataSource.INS.setAutomodLogChannel(guild.getId(), null);
                        }, null);

            } catch (Exception ex) { /* Ignore */ }
        }

    }

    private boolean shouldModerate(Member member, TextChannel channel) {
        final Member selfMember = member.getGuild().getSelfMember();

        // Ignore if bot cannot delete channel messages
        if (!selfMember.hasPermission(channel, Permission.MESSAGE_MANAGE))
            return false;

        // Ignore users with whom bot cannot interact
        if (!selfMember.canInteract(member))
            return false;

        // Ignore Possible Guild Moderators
        if (member.hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS, Permission.MANAGE_SERVER))
            return false;

        // Ignore Possible Channel Moderators
        return !member.hasPermission(channel, Permission.MESSAGE_MANAGE);

    }

}