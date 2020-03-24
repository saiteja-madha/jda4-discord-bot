package bot.main;

import java.sql.SQLException;

import javax.security.auth.login.LoginException;

import bot.database.SQLiteDataSource;
import bot.main.listeners.*;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {

	private Bot() throws LoginException, SQLException {
		
		SQLiteDataSource.getConnection();

    	WebUtils.setUserAgent("Beta Bot");
		EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                .setColor(0x3883d9)
                .setFooter("Beta Bot")
        );

		new JDABuilder().setToken(Config.get("token"))
				.addEventListeners(new ReadyListener())
				.addEventListeners(new GuildMessageListener())
				.addEventListeners(new ReactionListener())
				.setActivity(Activity.watching("this discord")).build();

	}

	public static void main(String[] args) throws LoginException, SQLException {
		new Bot();
	}

}
