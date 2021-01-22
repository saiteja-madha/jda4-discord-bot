package bot;

import bot.handlers.CommandHandler;
import bot.handlers.ReactionHandler;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bot {

    private final ScheduledExecutorService threadpool;
    private final EventWaiter waiter;
    private final CommandHandler cmdHandler;
    private final ReactionHandler reactionHandler;

    private Bot() throws LoginException {

        threadpool = Executors.newScheduledThreadPool((Config.getInt("threadpool_size")));
        waiter = new EventWaiter();
        cmdHandler = new CommandHandler(waiter);
        reactionHandler = new ReactionHandler();

        WebUtils.setUserAgent("Beta Bot");
        EmbedUtils.setEmbedBuilder(() -> new EmbedBuilder()
                .setColor(0x3883d9)
                .setFooter("Beta Bot")
        );

        new JDABuilder().setToken(Config.get("token"))
                .addEventListeners(waiter, new Listener(this))
                .setActivity(Activity.watching("this discord"))
                .build();

    }

    public static void main(String[] args) throws LoginException {
        new Bot();
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public CommandHandler getCmdHandler() {
        return cmdHandler;
    }

    public ReactionHandler getReactionHandler() {
        return reactionHandler;
    }

}
