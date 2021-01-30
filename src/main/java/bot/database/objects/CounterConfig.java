package bot.database.objects;

import org.bson.Document;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CounterConfig {

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
        this.tCountName = doc.getString("total_count_name");
        this.mCountName = doc.getString("member_count_name");
        this.bCountName = doc.getString("bot_count_name");
        this.botCount = doc.get("bot_count", 0);
    }

    public CounterConfig(ResultSet rs) throws SQLException {
        this.tCountChannel = rs.getString("total_count_channel");
        this.mCountChannel = rs.getString("member_count_channel");
        this.bCountChannel = rs.getString("bot_count_channel");
        this.tCountName = rs.getString("total_count_name");
        this.mCountName = rs.getString("member_count_name");
        this.bCountName = rs.getString("bot_count_name");
        this.botCount = rs.getInt("bot_count");
    }

}
