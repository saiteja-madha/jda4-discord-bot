package bot.data;

import org.jetbrains.annotations.Nullable;

public enum GreetingType {

    WELCOME("Welcome"),
    FAREWELL("Farewell");

    private final String search;

    GreetingType(String text) {
        this.search = text;
    }

    @Nullable
    public static GreetingType fromSearch(String text) {
        for (GreetingType b : GreetingType.values()) {
            if (b.search.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public String getText() {
        return this.search;
    }

    public String getAttachmentName() {
        return this.search.toLowerCase() + ".png";
    }

}
