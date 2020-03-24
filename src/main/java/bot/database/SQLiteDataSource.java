package bot.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import bot.main.Config;


public class SQLiteDataSource {
    private static final Logger LOGGER =  LoggerFactory.getLogger(SQLiteDataSource.class);
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {
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

        config.setJdbcUrl("jdbc:sqlite:database.db");
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
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

    private SQLiteDataSource() { }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
