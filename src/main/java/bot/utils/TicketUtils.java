package bot.utils;

import bot.Constants;
import bot.database.DataSource;
import bot.database.objects.Ticket;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TicketUtils {

    public static final Permission[] PERMS = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE};

    public static final String OPEN_MSG = "Hello %s!"
            + "\nSupport will be with you shortly"
            + "\n\n" + "**Ticket Reason:**"
            + "\n" + "%s";

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketUtils.class);

    public static User getTicketOpenUser(TextChannel channel) {
        final String userId = Objects.requireNonNull(channel.getTopic()).split("\\|")[1];

        try {
            return channel.getJDA().retrieveUserById(userId).submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(String.format("Failed to fetch user with ID: %s | Error: %s", userId, e.getMessage()));
        }

        return null;

    }

    // Cyrillic "і" is used
    public static boolean isTicketChannel(TextChannel channel) {
        return channel.getName().startsWith("tіcket-") && channel.getTopic() != null
                && channel.getTopic().startsWith("tіcket|");
    }

    public static void closeTicket(String reason, Guild guild, User closed, TextChannel tc) {
        final Ticket config = DataSource.INS.getTicketConfig(guild.getId());
        TextChannel lc = null;

        if (config != null) {
            if (config.logChannelId != null) {
                TextChannel tcById = guild.getTextChannelById(config.logChannelId);
                if (tcById != null)
                    lc = tcById;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Strange Ticket Logs");
        sb.append("\n");
        sb.append(" User | Message | Timestamp");
        sb.append("\n");
        sb.append("--- | --- | ---");
        sb.append("\n");

        List<Message> msgs = new ArrayList<>();
        for (Message msg : tc.getIterableHistory())
            msgs.add(msg);

        Collections.reverse(msgs);

        for (Message msg : msgs) {

            if (msg.getAuthor().isBot())
                continue;

            StringBuilder content = new StringBuilder(MiscUtils.escapeString(msg.getContentDisplay()));
            List<Attachment> attachments = msg.getAttachments();

            if (attachments.size() > 0)
                for (Attachment attachment : attachments)
                    content.append(" [attachment](")
                            .append(attachment.getUrl()).append(")");

            sb.append(MiscUtils.escapeString(msg.getAuthor().getName()))
                    .append(" | ")
                    .append(content)
                    .append(" | ")
                    .append(msg.getTimeCreated().toLocalDateTime())
                    .append("\n");
        }

        final User opened = getTicketOpenUser(tc);
        if (opened != null) {
            sb.append("\n\n");
            sb.append("Opened by ").append(MiscUtils.escapeString(opened.getAsTag()));
        }

        if (closed != null) {
            sb.append("\n\n");
            sb.append("Closed by ").append(MiscUtils.escapeString(closed.getAsTag()));
        }

        JSONObject jsonBody = new JSONObject().put("description", "Ticket Log").put("public", false).put("files",
                new JSONObject().put("ticket-log.md", new JSONObject().put("content", String.valueOf(sb))));

        final TextChannel logChannel = lc;

        try {
            tc.delete().submit().get();
            final String logUrl = HttpUtils.postLogsToGist(jsonBody.toString());
            String logsString = (logUrl == null) ? "" : "[View Logs](" + logUrl + ")";

            String dmEmbedDesc = Constants.WHITE_SMALL_SQUARE + "**Server Name:** " + MiscUtils.escapeString(guild.getName()) + "\n"
                    + Constants.WHITE_SMALL_SQUARE + "**Opened by:** " + (opened != null ? MiscUtils.escapeString(opened.getAsTag()) : "User left") + "\n"
                    + Constants.WHITE_SMALL_SQUARE + "**Closed by:** " + (closed != null ? MiscUtils.escapeString(closed.getAsTag()) : "User left") + "\n"
                    + Constants.WHITE_SMALL_SQUARE + "**Reason:** " + (reason != null ? MiscUtils.escapeString(reason) : "No reason provided") + "\n\n"
                    + logsString;

            EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setAuthor("Ticket Closed");

            if (logChannel != null) {
                String lcEmbedDesc = ""
                        + Constants.WHITE_SMALL_SQUARE + "**Opened by:** " + (opened != null ? MiscUtils.escapeString(opened.getAsTag()) : "User left") + "\n"
                        + Constants.WHITE_SMALL_SQUARE + "**Closed by:** " + (closed != null ? MiscUtils.escapeString(closed.getAsTag()) : "User left") + "\n"
                        + Constants.WHITE_SMALL_SQUARE + "**Reason:** " + (reason != null ? MiscUtils.escapeString(reason) : "No reason provided") + "\n\n"
                        + logsString;

                BotUtils.sendMsg(logChannel, embed.setDescription(lcEmbedDesc).build());
            }

            BotUtils.sendDM(opened, embed.setDescription(dmEmbedDesc).build());

        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage());
        }

    }

    public static void handleNewTicket(Guild guild, Member member, String reason, String roleId, int existing)
            throws InterruptedException, ExecutionException {

        final String ticketNumber = String.valueOf(existing + 1);

        // Cyrillic "і" is used
        ChannelAction<TextChannel> action = guild.createTextChannel("tіcket-" + ticketNumber)
                .setTopic("tіcket" + "|" + member.getId())
                .addPermissionOverride(member, Arrays.asList(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE),
                        Collections.emptyList())
                .addPermissionOverride(guild.getPublicRole(), Collections.emptyList(),
                        Arrays.asList(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE))
                .addPermissionOverride(Objects.requireNonNull(guild.getMember(guild.getSelfMember().getUser())),
                        Arrays.asList(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE), Collections.emptyList());

        if (roleId != null) {
            action = action.addPermissionOverride(Objects.requireNonNull(guild.getRoleById(roleId)),
                    Arrays.asList(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE), Collections.emptyList());
        }

        CompletableFuture<TextChannel> submit = action.submit();

        EmbedBuilder meb = EmbedUtils.defaultEmbed()
                .setAuthor("Ticket #" + ticketNumber)
                .setDescription(String.format(OPEN_MSG, MiscUtils.escapeString(member.getUser().getAsTag()),
                        (reason != null ? reason : "No reason provided!")))
                .setFooter("To close your ticket react to the lock below");

        final TextChannel tc = submit.get();

        Message msg = tc.sendMessage(meb.build()).submit().get();
        msg.addReaction(Constants.LOCK).queue();
        tc.sendMessage(member.getAsMention()).queue();

        EmbedBuilder eb = EmbedUtils.defaultEmbed()
                .setAuthor("Ticket Created")
                .setDescription(""
                        + Constants.WHITE_SMALL_SQUARE + "**Ticket:** #" + ticketNumber + "\n"
                        + Constants.WHITE_SMALL_SQUARE + "**Server Name:** " + guild.getName() + "\n"
                        + Constants.WHITE_SMALL_SQUARE + "**Reason:** " + (reason != null ? reason : "No reason provided!") + "\n\n"
                        + "[View Channel](" + msg.getJumpUrl() + ")");

        BotUtils.sendDM(member.getUser(), eb.build());

    }

    public static int closeAllTickets(Guild guild) {
        List<TextChannel> textChannels = guild.getTextChannels();
        int count = 0;
        for (TextChannel tc : textChannels) {
            if (isTicketChannel(tc)) {
                closeTicket("Force close all open tickets", guild, guild.getJDA().getSelfUser(), tc);
                count++;
            }
        }
        return count;
    }

    public static int getExistingTickets(Guild guild) {
        List<TextChannel> textChannels = guild.getTextChannels();
        int count = 0;

        // Cyrillic "і" is used
        for (TextChannel t : textChannels) {
            if (t.getName().startsWith("tіcket-") && t.getTopic() != null && t.getTopic().startsWith("tіcket|"))
                count++;
        }
        return count;
    }

    @Nullable
    public static TextChannel getExistingTicketChannel(Guild guild, User user) {
        String userId = user.getId();
        List<TextChannel> textChannels = guild.getTextChannels();
        for (TextChannel t : textChannels) {
            if (t.getName().startsWith("tіcket-") && t.getTopic() != null && t.getTopic().startsWith("tіcket|")) {
                if (userId.equalsIgnoreCase(t.getTopic().split("\\|")[1])) {
                    return t;
                }
            }
        }
        return null;
    }

    private static String randomTicket() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 4;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

}
