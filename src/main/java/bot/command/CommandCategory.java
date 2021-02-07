package bot.command;

import org.jetbrains.annotations.Nullable;

public enum CommandCategory {

    ADMINISTRATION("Admin"),
    ECONOMY("Economy"),
    FUN("Fun", "https://cdn4.iconfinder.com/data/icons/instagram-highlight/64/just-for-fun-512.png"),
    IMAGE("Image", "https://www.freeiconspng.com/uploads/multimedia-photo-icon-31.png"),
    INFORMATION("Information", "https://freepngimg.com/thumb/logo/88430-information-icons-text-wallpaper-question-computer.png"),
    MODERATION("Moderation", "https://cdn3.iconfinder.com/data/icons/web-marketing-1-3/48/30-512.png"),
    OWNER("Owner"),
    SOCIAL("Social", "https://cdn1.iconfinder.com/data/icons/business-and-finance-2-5/130/99-512.png"),
    UNLISTED(null),
    UTILS("Utility", "https://icons.iconseeker.com/png/fullsize/macintag-folders-white-edge/white-utility.png");

    private final String search;
    private final String iconUrl;

    CommandCategory(String search) {
        this.search = search;
        this.iconUrl = null;
    }

    CommandCategory(String search, String url) {
        this.search = search;
        this.iconUrl = url;
    }

    public static @Nullable CommandCategory fromSearch(String input) {
        for (final CommandCategory value : values()) {
            if (input.equalsIgnoreCase(value.name()) || input.equalsIgnoreCase(value.getSearch())) {
                return value;
            }
        }
        return null;
    }

    public String getSearch() {
        return search;
    }

    public String getIconUrl() {
        return iconUrl;
    }

}