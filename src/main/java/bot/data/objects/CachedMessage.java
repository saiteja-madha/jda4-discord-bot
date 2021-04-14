package bot.data.objects;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.time.OffsetDateTime;

public class CachedMessage {

    private final String content;
    private final int mentions;
    private final String channelId;
    private final User author;
    private final OffsetDateTime timeSent;

    public CachedMessage(Message message) {
        this.content = message.getContentRaw();
        this.mentions = message.getMentionedMembers().size();
        this.channelId = message.getTextChannel().getId();
        this.author = message.getAuthor();
        this.timeSent = message.getTimeCreated();
    }

    public String getContent() {
        return content;
    }

    public int getMentions() {
        return mentions;
    }

    public String getChannelId() {
        return channelId;
    }

    public User getAuthor() {
        return author;
    }

    public OffsetDateTime getTimeSent() {
        return timeSent;
    }

}
