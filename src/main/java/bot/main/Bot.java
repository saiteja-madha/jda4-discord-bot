package bot.main;

import bot.main.listeners.GuildMessageListener;
import bot.main.listeners.ReactionListener;
import bot.main.listeners.ReadyListener;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class Bot {

    private Bot() throws LoginException {

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
                .setActivity(Activity.watching("this discord"))
                .build();

    }

    public static void main(String[] args) throws LoginException {
        new Bot();
    }

}
