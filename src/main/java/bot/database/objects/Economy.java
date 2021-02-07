package bot.database.objects;

import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class Economy {

    public final int coins;
    @Nullable
    public final Instant dailyTimestamp;
    public final int dailyStreak;

    public Economy() {
        this.coins = 0;
        this.dailyTimestamp = null;
        this.dailyStreak = 0;
    }

    public Economy(Document doc) {
        this.coins = doc.get("coins", 0);
        this.dailyTimestamp = doc.containsKey("daily_timestamp") ? doc.getDate("daily_timestamp").toInstant() : null;
        this.dailyStreak = doc.get("daily_streak", 0);
    }

}
