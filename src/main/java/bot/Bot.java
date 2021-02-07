package bot;

import bot.handlers.*;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bot {

    private final ScheduledExecutorService threadpool;
    private final EventWaiter waiter;
    private final CommandHandler cmdHandler;
    private final ReactionHandler reactionHandler;
    private final MemberHandler memberHandler;
    private final XPHandler xpHandler;
    private final AutoModHandler automodHandler;

    private Bot() throws LoginException {

        threadpool = Executors.newScheduledThreadPool((Config.getInt("threadpool_size")));
        waiter = new EventWaiter();
        cmdHandler = new CommandHandler(waiter);
        reactionHandler = new ReactionHandler();
        memberHandler = new MemberHandler(this);
        xpHandler = new XPHandler();
        automodHandler = new AutoModHandler();

        EmbedUtils.setEmbedBuilder(() -> new EmbedBuilder()
                .setColor(Constants.BOT_EMBED)
                .setFooter("Strange Bot")
        );

        JDABuilder.createDefault(Config.get("TOKEN"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                .addEventListeners(waiter, new Listener(this))
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Booting..."))
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

    public MemberHandler getMemberHandler() {
        return memberHandler;
    }

    public XPHandler getXpHandler() {
        return xpHandler;
    }

    public AutoModHandler getAutomodHandler() {
        return automodHandler;
    }

}
