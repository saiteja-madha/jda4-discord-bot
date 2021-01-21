package bot.command;

import bot.utils.BotUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class CommandContext {

    private final GuildMessageReceivedEvent event;
    private final String prefix;
    private final String invoke;
    private final List<String> args;

    public CommandContext(GuildMessageReceivedEvent event, List<String> args, String invoke, String prefix) {
        this.event = event;
        this.prefix = prefix;
        this.invoke = invoke;
        this.args = args;
    }

    public GuildMessageReceivedEvent getEvent() {
        return this.event;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getInvoke() {
        return this.invoke;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public Guild getGuild() {
        return this.getEvent().getGuild();
    }

    public TextChannel getChannel() {
        return this.getEvent().getChannel();
    }

    public Message getMessage() {
        return this.getEvent().getMessage();
    }

    public Member getMember() {
        return this.getEvent().getMember();
    }

    public Member getSelfMember() {
        return this.getGuild().getSelfMember();
    }

    public JDA getJDA() {
        return this.getEvent().getJDA();
    }

    public User getAuthor() {
        return this.getEvent().getAuthor();
    }

    public String getArgsJoined() {
        return String.join(" ", this.getArgs());
    }

    public void reply(String message) {
        BotUtils.sendMsg(this.getChannel(), message);
    }

    public void reply(MessageEmbed embed) {
        BotUtils.sendEmbed(this.getChannel(), embed);
    }

}
