package bot.database.sqlite;

import bot.Config;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.*;


public class SQLiteDS {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDS.class);
    private final HikariDataSource ds;

    public SQLiteDS() {
        try {
            final File dbFile = new File("database.db");

            if (!dbFile.exists()) {
                if (dbFile.createNewFile()) {
                    LOGGER.info("Created database file");
                } else {
                    LOGGER.info("Could not create database file");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig("src/main/resources/sqlite.properties");
        ds = new HikariDataSource(config);

        try (final Statement statement = getConnection().createStatement()) {
            final String defaultPrefix = Config.get("prefix");

            statement.execute("CREATE TABLE IF NOT EXISTS guild_settings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guild_id VARCHAR(20) NOT NULL," +
                    "rr_enabled VARCHAR(2) NOT NULL DEFAULT 'N'," +
                    "prefix VARCHAR(255) NOT NULL DEFAULT '" + defaultPrefix + "'" +
                    ");");

            LOGGER.info("guild_settings Table initialised");

            statement.execute("CREATE TABLE IF NOT EXISTS reaction_roles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guild_id integer NOT NULL," +
                    "channel_id integer NOT NULL," +
                    "message_id integer NOT NULL," +
                    "emote VARCHAR(255) NOT NULL," +
                    "role_id integer NOT NULL);");

            LOGGER.info("reaction_roles Table initialised");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public GuildSettings getSettings(long guildId) {
        try (final PreparedStatement preparedStatement = getConnection()
                .prepareStatement("SELECT prefix FROM guild_settings WHERE guild_id = ?")) {

            preparedStatement.setString(1, String.valueOf(guildId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new GuildSettings(resultSet);
                }
            }

            try (final PreparedStatement insertStatement = getConnection()
                    .prepareStatement("INSERT INTO guild_settings(guild_id) VALUES(?)")) {

                insertStatement.setString(1, String.valueOf(guildId));

                insertStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new GuildSettings();
    }

    public void setPrefix(long guildId, String newPrefix) {

        try (final PreparedStatement preparedStatement = getConnection()
                .prepareStatement("UPDATE guild_settings SET prefix = ? WHERE guild_id = ?")) {

            preparedStatement.setString(1, newPrefix);
            preparedStatement.setString(2, String.valueOf(guildId));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addReactionRole(long guildId, long channelId, long messageId, long roleId, String emote) {
        try (final PreparedStatement insertStatement = getConnection()
                .prepareStatement("INSERT INTO reaction_roles (guild_id, channel_id, message_id, emote, role_id) VALUES (?, ?, ?, ?, ?)")) {

            insertStatement.setLong(1, guildId);
            insertStatement.setLong(2, channelId);
            insertStatement.setLong(3, messageId);
            insertStatement.setString(4, emote);
            insertStatement.setLong(5, roleId);

            insertStatement.executeUpdate();

            try (final PreparedStatement updateStatement = getConnection()
                    .prepareStatement("UPDATE guild_settings SET rr_enabled = 'Y' WHERE guild_id = ?")) {

                updateStatement.setLong(1, guildId);
                updateStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeReactionRole(long guildId, long channelId, long messageId, String emote) throws SQLException {
        try (final PreparedStatement insertStatement = getConnection()
                .prepareStatement("DELETE FROM reaction_roles WHERE guild_id = ? AND channel_id = ? AND message_id = ?")) {

            insertStatement.setLong(1, guildId);
            insertStatement.setLong(2, channelId);
            insertStatement.setLong(3, messageId);

            insertStatement.executeUpdate();

            try (final PreparedStatement updateStatement = getConnection()
                    .prepareStatement("UPDATE guild_settings SET rr_enabled = 'Y' WHERE guild_id = ?")) {

                updateStatement.setLong(1, guildId);
                updateStatement.executeUpdate();

            }
        }
    }

    public long getReactionRoleId(long guildId, long channelId, long messageId, String emote) throws Exception {
        try (Connection conn = getConnection();
             final PreparedStatement preparedStatement = conn
                     .prepareStatement("SELECT role_id FROM reaction_roles WHERE guild_id = ? AND channel_id = ? AND message_id = ? AND emote = ?")) {

            preparedStatement.setLong(1, guildId);
            preparedStatement.setLong(2, channelId);
            preparedStatement.setLong(3, messageId);
            preparedStatement.setString(4, emote);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("role_id");
                }
            }

        }
        return 0;
    }
}
