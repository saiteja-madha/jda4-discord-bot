package bot.database.objects;

import org.bson.Document;

import java.util.Date;

public class WarnLogs {

    public final String memberId, modId;
    public final String modName;
    public final String modReason;
    public final Date timeStamp;

    public WarnLogs(Document doc) {
        this.memberId = doc.getString("member_id");
        this.modId = doc.getString("moderator_id");
        this.modName = doc.getString("moderator_name");
        this.modReason = doc.getString("reason");
        this.timeStamp = doc.getDate("time_stamp");
    }

}
