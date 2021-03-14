package bot.handlers;

import bot.Bot;
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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ReactionHandler extends ListenerAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(ReactionHandler.class);
    private final Bot bot;

    public ReactionHandler(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        DataSource.INS.removeReactionRole(event.getGuild().getId(), event.getChannel().getId(), event.getMessageId());
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        if (event.getReactionEmote().isEmoji()) {
            bot.getReactionHandler().handleFlagReaction(event);

            // Handle Tickets in async
            bot.getThreadpool().execute(() -> bot.getReactionHandler().handleTicket(event));
        }

        bot.getReactionHandler().handleReactionRole(event, true);

    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if (event.getUser() != null && event.getUser().isBot())
            return;

        bot.getReactionHandler().handleReactionRole(event, false);

    }

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

        if (role == null) {
            // TODO: If role is removed, remove data from DB
            return;
        }

        try {
            if (isAdded)
                guild.addRoleToMember(event.getUserId(), role).queue(
                        (__) -> sendRoleInfo(event.getUser(), guild, role, true),
                        e -> LOGGER.error("ReactionRole - Reaction Add failed : " + e.getMessage()));
            else
                guild.removeRoleFromMember(event.getUserId(), role).queue(
                        (__) -> sendRoleInfo(event.getUser(), guild, role, false),
                        e -> LOGGER.error("ReactionRole - Reaction Remove failed : " + e.getMessage()));

        } catch (PermissionException ex) { /* Ignore */ } catch (Exception e) {
            LOGGER.error("ReactionRole failed : " + e.getMessage());
        }

    }

    private void sendRoleInfo(User user, Guild guild, Role role, boolean isAdded) {
        String SERVER_LINK = "https://discord.com/channels/";
        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setDescription("**Guild Name**: " + BotUtils.getEmbedHyperLink(guild.getName(), SERVER_LINK + guild.getId()) +
                        "\n**Role Name**: " + role.getName()
                );

        if (isAdded) {
            embed.setAuthor("Role Added")
                    .setColor(role.getColor());
        } else {
            embed.setAuthor("Role Removed")
                    .setColor(Constants.ERROR_EMBED);
        }

        BotUtils.sendDM(user, embed.build());
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
        if (!config.translationChannels.contains(event.getChannel().getId()))
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

            EmbedBuilder eb = EmbedUtils.getDefaultEmbed()
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
