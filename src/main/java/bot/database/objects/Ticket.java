package bot.database.objects;

import org.bson.Document;

public class Ticket {

    public final String channelId;
    public final String messageId;
    public final String title;
    public final String roleId;
    public final String logChannelId;
    public final int ticketLimit;
    public final boolean adminOnly;

    public Ticket(Document doc) {
        this.channelId = doc.getString("channel_id");
        this.messageId = doc.getString("message_id");
        this.title = doc.getString("title");
        this.roleId = doc.getString("support_role");
        this.logChannelId = doc.getString("log_channel");
        this.ticketLimit = doc.getInteger("ticket_limit", 10);
        this.adminOnly = doc.get("admin_only", false);
    }

}
