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

    public Economy(Document document) {
        this.coins = document.containsKey("coins") ? document.getInteger("coins") : 0;
        this.dailyTimestamp = document.containsKey("daily_timestamp") ? document.getDate("daily_timestamp").toInstant() : null;
        this.dailyStreak = document.containsKey("daily_streak") ? document.getInteger("daily_streak") : 0;
    }

}
