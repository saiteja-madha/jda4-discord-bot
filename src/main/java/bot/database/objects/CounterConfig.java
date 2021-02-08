package bot.database.objects;

import org.bson.Document;
import org.jetbrains.annotations.Nullable;

public class CounterConfig {

    @Nullable
    public final String tCountChannel, mCountChannel, bCountChannel;
    public final String tCountName, mCountName, bCountName;
    public final int botCount;

    public CounterConfig() {
        this.tCountChannel = null;
        this.mCountChannel = null;
        this.bCountChannel = null;
        this.tCountName = null;
        this.mCountName = null;
        this.bCountName = null;
        this.botCount = 0;
    }

    public CounterConfig(Document doc) {
        this.tCountChannel = doc.getString("total_count_channel");
        this.mCountChannel = doc.getString("member_count_channel");
        this.bCountChannel = doc.getString("bot_count_channel");
        this.tCountName = doc.get("total_count_name", "Members & Bots");
        this.mCountName = doc.get("member_count_name", "Members");
        this.bCountName = doc.get("bot_count_name", "Bots");
        this.botCount = doc.get("bot_count", 0);
    }

}
