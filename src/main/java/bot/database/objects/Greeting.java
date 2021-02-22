package bot.database.objects;

import bot.data.GreetingType;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

public class Greeting {

    @Nullable
    public final String channel;
    @Nullable
    public final String description;
    @Nullable
    public final String embedFooter;
    @Nullable
    public final String embedColor;
    public final boolean embedThumbnail;
    @Nullable
    public final String embedImage;
    public final GreetingType type;

    public Greeting(Document doc, GreetingType type) {
        this.channel = doc.getString("channel_id");
        if (type == GreetingType.WELCOME) {
            this.description = doc.get("description", "Welcome to our server {member} ");
            this.embedColor = doc.get("embed_color", "#1abc9c");
        } else {
            this.description = doc.get("description", "{member} has left {server}");
            this.embedColor = doc.get("embed_color", "#eb4d4b");
        }
        this.embedFooter = doc.getString("embed_footer");
        this.embedThumbnail = doc.get("embed_thumbnail", false);
        this.embedImage = doc.getString("embed_image");
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
