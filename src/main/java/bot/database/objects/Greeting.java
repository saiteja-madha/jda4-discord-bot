package bot.database.objects;

import bot.data.GreetingType;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

public class Greeting {

    public final boolean isEmbedEnabled;
    public final boolean isImageEnabled;
    @Nullable
    public final String channel;
    @Nullable
    public final String description;
    @Nullable
    public final String footer;
    @Nullable
    public final String embedColor;
    @Nullable
    public final String imageMessage;
    @Nullable
    public final String imageBkg;
    public final GreetingType type;

    public Greeting(Document doc, GreetingType type) {
        this.isEmbedEnabled = doc.getBoolean("embed_enabled", true);
        this.channel = doc.getString("channel_id");
        this.description = doc.getString("description");
        this.footer = doc.getString("footer");
        this.embedColor = doc.getString("embed_color");
        this.isImageEnabled = doc.getBoolean("image_enabled", true);
        this.imageMessage = doc.getString("image_message");
        this.imageBkg = doc.getString("image_background");
        this.type = type;
    }


    public static class Welcome extends Greeting {

        public Welcome(Document doc) {
            super(doc, GreetingType.WELCOME);
        }

    }

    public static class Farewell extends Greeting {

        public Farewell(Document doc) {
            super(doc, GreetingType.FAREWELL);
        }

    }


}
