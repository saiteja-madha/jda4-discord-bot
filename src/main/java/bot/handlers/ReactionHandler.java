package bot.handlers;

import bot.Constants;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.database.objects.Ticket;
import bot.utils.BotUtils;
import bot.utils.HttpUtils;
import bot.utils.TicketUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ReactionHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(ReactionHandler.class);

    public void handleReactionRole(@NotNull GenericGuildMessageReactionEvent event, boolean isAdded) {
        final MessageReaction.ReactionEmote reactionEmote = event.getReaction().getReactionEmote();
        final Guild guild = event.getGuild();
        final TextChannel channel = event.getChannel();

        String emoji;
        if (reactionEmote.isEmote())
            emoji = reactionEmote.getEmote().getName() + ":" + reactionEmote.getEmote().getId();
        else
            emoji = reactionEmote.getEmoji();

        String roleId = DataSource.INS.getReactionRoleId(guild.getId(), channel.getId(), event.getMessageId(), emoji);

        if (roleId == null)
            return;

        final Role role = guild.getRoleById(roleId);

        // If role is removed, remove data from DB
        if (role == null) {
            DataSource.INS.removeReactionRole(guild.getId(), channel.getId(), event.getMessageId(), emoji);
            return;
        }

        try {
            if (isAdded)
                guild.addRoleToMember(event.getUserId(), role).queue(
                        (__) -> BotUtils.sendDM(event.getUser(), "**Reaction Role:** You are given `" + role.getName() + "` role in " + guild.getName()),
                        e -> LOGGER.error("ReactionRole - Reaction Add failed : " + e.getMessage()));
            else
                guild.removeRoleFromMember(event.getUserId(), role).queue(
                        (__) -> BotUtils.sendDM(event.getUser(), "**Reaction Role:** Removed role `" + role.getName() + "` in " + guild.getName()),
                        e -> LOGGER.error("ReactionRole - Reaction Remove failed : " + e.getMessage()));

        } catch (PermissionException ex) { /* Ignore */ } catch (Exception e) {
            LOGGER.error("ReactionRole failed : " + e.getMessage());
        }

    }

    public void handleFlagReaction(@NotNull GuildMessageReactionAddEvent event) {
        final Guild guild = event.getGuild();
        final TextChannel tc = event.getChannel();
        final String messageId = event.getMessageId();

        String unicode = event.getReactionEmote().toString().replace("RE:", "");

        if (unicode.length() != 14) {
            return;
        }

        // Check Guild Configuration
        GuildSettings config = DataSource.INS.getSettings(event.getGuild().getId());
        if (!config.flagTranslation || !config.translationChannels.contains(event.getChannel().getId()))
            return;

        // Check if already translated
        if (DataSource.INS.isTranslated(tc.getGuild().getId(), tc.getId(), messageId, unicode))
            return;

        String l1 = Constants.flagCodes.get(unicode.substring(0, 7).toUpperCase());
        String l2 = Constants.flagCodes.get(unicode.substring(7, 14).toUpperCase());

        Locale found = null;
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.getCountry().equalsIgnoreCase(l1 + l2)) {
                found = l;
                break;
            }
        }

        if (found == null)
            return;

        final String outputCode = found.getLanguage();
        tc.retrieveMessageById(messageId).queue((message) -> {

            if (!message.getEmbeds().isEmpty()) {
                return;
            }

            final String input = message.getContentStripped();
            final String[] translate = HttpUtils.translate(outputCode, input);

            if (translate == null) {
                BotUtils.sendMsg(tc, "No translation found");
                return;
            }

            EmbedBuilder eb = EmbedUtils.defaultEmbed()
                    .setDescription("**Translation:** \n" + translate[5] + "\n\n" +
                            "[view original](" + message.getJumpUrl() + ")")
                    .setFooter(translate[2] + " (" + translate[0] + ")" + " ⟶ " + translate[3] + " (" + translate[1] + ")\n" +
                            "Reacted By: " + event.getUser().getAsTag());

            tc.sendMessage(eb.build()).queue((__) ->
                    DataSource.INS.addTranslation(guild.getId(), tc.getId(), messageId, unicode)
            );

        }, e -> LOGGER.error("Flag Translation Failed: " + e.getMessage()));
    }

    public void handleTicket(@NotNull GuildMessageReactionAddEvent event) {
        final String emoji = event.getReactionEmote().getEmoji();

        if (emoji.equalsIgnoreCase(Constants.ENVELOPE_WITH_ARROW))
            this.handleTicketCreation(event);

        else if (emoji.equalsIgnoreCase(Constants.LOCK))
            this.handleTicketClose(event);

    }

    private void handleTicketCreation(@NotNull GuildMessageReactionAddEvent event) {
        final Guild guild = event.getGuild();
        final String messageId = event.getMessageId();
        final String channelId = event.getChannel().getId();
        Ticket config = DataSource.INS.getTicketConfig(guild.getId());

        if (config == null)
            return;

        if (!messageId.equalsIgnoreCase(config.messageId) && !channelId.equalsIgnoreCase(config.channelId))
            return;

        final int tkts = TicketUtils.getExistingTickets(guild);

        if (tkts > config.ticketLimit) {
            BotUtils.sendMsg(event.getChannel(), "Ticket limit reached! try again later", 3);
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        TextChannel existingTicketChannel = TicketUtils.getExistingTicketChannel(guild, event.getUser());

        if (existingTicketChannel != null) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        try {
            TicketUtils.handleNewTicket(guild, event.getMember(), config.title, config.roleId, tkts);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Ticket Creation Failed: " + e.getMessage());
        }

        event.getReaction().removeReaction(event.getUser()).queue();

    }

    private void handleTicketClose(@NotNull GuildMessageReactionAddEvent event) {
        final Guild guild = event.getGuild();
        final TextChannel channel = event.getChannel();
        Ticket config = DataSource.INS.getTicketConfig(guild.getId());

        if (config != null && config.adminOnly) {
            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                BotUtils.sendMsg(channel, "Ticket can only be closed by admin!");
                return;
            }
        }

        if (TicketUtils.isTicketChannel(channel))
            TicketUtils.closeTicket("Reacted with emoji", guild, event.getUser(), channel);

    }

}
