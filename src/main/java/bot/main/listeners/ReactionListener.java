package bot.main.listeners;

import bot.database.DataSource;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class ReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot() || !event.getReactionEmote().isEmoji())
            return;

        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        long messageId = event.getMessageIdLong();
        String emote = event.getReactionEmote().getEmoji();

        try {
            long roleId = DataSource.INS.getReactionRoleId(guildId, channelId, messageId, emote);
            if (roleId != 0) {
                Role role = event.getGuild().getRoleById(roleId);
                if (role == null)
                    DataSource.INS.removeReactionRole(guildId, channelId, messageId, emote);
                else
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if (event.getUser() == null || event.getMember() == null || event.getUser().isBot() || !event.getReactionEmote().isEmoji())
            return;

        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        long messageId = event.getMessageIdLong();
        String emote = event.getReactionEmote().getEmoji();

        try {
            long roleId = DataSource.INS.getReactionRoleId(guildId, channelId, messageId, emote);
            if (roleId != 0) {
                Role role = event.getGuild().getRoleById(roleId);
                if (role == null)
                    DataSource.INS.removeReactionRole(guildId, channelId, messageId, emote);
                else
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
