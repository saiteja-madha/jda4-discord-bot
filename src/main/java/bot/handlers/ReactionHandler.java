package bot.handlers;

import bot.Constants;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.utils.BotUtils;
import bot.utils.HttpUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
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

        String roleId = DataSource.INS.getReactionRoleId(guild.getIdLong(), channel.getId(), event.getMessageId(), emoji);

        if (roleId == null)
            return;

        final Role role = guild.getRoleById(roleId);

        // If role is removed, remove data from DB
        if (role == null) {
            DataSource.INS.removeReactionRole(guild.getIdLong(), channel.getId(), event.getMessageId(), emoji);
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
        final long messageId = event.getMessageIdLong();

        String unicode = event.getReactionEmote().toString().replace("RE:", "");

        if (unicode.length() != 14) {
            return;
        }

        // Check Guild Configuration
        GuildSettings config = DataSource.INS.getSettings(event.getGuild().getIdLong());
        if (!config.flagTranslation || !config.translationChannels.contains(event.getChannel().getId()))
            return;

        // Check if already translated
        if (DataSource.INS.isTranslated(tc.getGuild().getIdLong(), tc.getIdLong(), messageId, unicode))
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
                    DataSource.INS.addTranslation(guild.getIdLong(), tc.getIdLong(), messageId, unicode)
            );

        }, e -> LOGGER.error("Flag Translation Failed: " + e.getMessage()));
    }

}
