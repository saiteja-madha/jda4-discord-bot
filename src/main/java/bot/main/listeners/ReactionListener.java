package bot.main.listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import bot.database.SQLiteDataSource;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		
		if (event.getChannelType() == ChannelType.TEXT) {
			
			long guildId = event.getGuild().getIdLong();
			
			if (!event.getUser().isBot()) {
				long channelId = event.getChannel().getIdLong();
				long messageId = event.getMessageIdLong();
				
				if (!event.getReactionEmote().isEmoji())
					return;
				
				String emote = event.getReactionEmote().getEmoji();

				try (Connection conn = SQLiteDataSource.getConnection();
						final PreparedStatement preparedStatement = conn
						.prepareStatement("SELECT role_id FROM reaction_roles WHERE guild_id = ? AND channel_id = ? AND message_id = ? AND emote = ?")) {

					preparedStatement.setLong(1, guildId);
					preparedStatement.setLong(2, channelId);
					preparedStatement.setLong(3, messageId);
					preparedStatement.setString(4, emote);

					try (final ResultSet resultSet = preparedStatement.executeQuery()) {
						if (resultSet.next()) {
							long roleId = resultSet.getLong("role_id");
							event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roleId))
									.queue();
						}
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {

		if (event.getChannelType() == ChannelType.TEXT) {
			
			long guildId = event.getGuild().getIdLong();
			
			if (!event.getUser().isBot() || event.getReactionEmote().isEmoji()) {
				long channelId = event.getChannel().getIdLong();
				long messageId = event.getMessageIdLong();
				String emote = event.getReactionEmote().getEmoji();

				try (Connection conn = SQLiteDataSource.getConnection();
						final PreparedStatement preparedStatement = conn
						.prepareStatement("SELECT role_id FROM reaction_roles WHERE guild_id = ? AND channel_id = ? AND message_id = ? AND emote = ?")) {

					preparedStatement.setLong(1, guildId);
					preparedStatement.setLong(2, channelId);
					preparedStatement.setLong(3, messageId);
					preparedStatement.setString(4, emote);

					try (final ResultSet resultSet = preparedStatement.executeQuery()) {
						if (resultSet.next()) {
							long roleId = resultSet.getLong("role_id");
							event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(roleId))
									.queue();
						}
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
