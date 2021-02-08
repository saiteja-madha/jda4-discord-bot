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
        if (type == GreetingType.WELCOME) {
            this.description = doc.get("description", "Welcome to our server {member} ");
            this.embedColor = doc.get("embed_color", "#1abc9c");
        } else {
            this.description = doc.get("description", "{member} has left {server}");
            this.embedColor = doc.get("embed_color", "#eb4d4b");
        }
        this.footer = doc.getString("footer");

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
