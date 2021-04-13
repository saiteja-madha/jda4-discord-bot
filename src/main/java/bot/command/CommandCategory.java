package bot.command;

import org.jetbrains.annotations.Nullable;

public enum CommandCategory {

    ADMINISTRATION("Admin", "https://icons.iconarchive.com/icons/dtafalonso/android-lollipop/512/Settings-icon.png", "\u2699"),
    AUTOMOD("Automod", "https://cdn3.iconfinder.com/data/icons/web-marketing-1-3/48/30-512.png", "\uD83D\uDEE1"),
    ECONOMY("Economy", "https://icons.iconarchive.com/icons/custom-icon-design/pretty-office-11/128/coins-icon.png", "\uD83E\uDE99"),
    FUN("Fun", "https://icons.iconarchive.com/icons/flameia/aqua-smiles/128/make-fun-icon.png", "\uD83D\uDE02"),
    IMAGE("Image", "https://icons.iconarchive.com/icons/dapino/summer-holiday/128/photo-icon.png", "\uD83D\uDDBC"),
    INFORMATION("Information", "https://icons.iconarchive.com/icons/graphicloads/100-flat/128/information-icon.png", "\uD83E\uDEA7"),
    INVITE("Invite", "https://icons.iconarchive.com/icons/streamlineicons/streamline-ux-free/128/user-female-add-icon.png", "\uD83D\uDCEC"),
    MODERATION("Moderation", "https://icons.iconarchive.com/icons/lawyerwordpress/law/128/Gavel-Law-icon.png", "\uD83D\uDD28"),
    OWNER("Owner"),
    SOCIAL("Social", "https://icons.iconarchive.com/icons/dryicons/aesthetica-2/128/community-users-icon.png", "\uD83E\uDEC2"),
    UNLISTED(null),
    UTILS("Utility", "https://icons.iconarchive.com/icons/blackvariant/button-ui-system-folders-alt/128/Utilities-icon.png", "\uD83D\uDEE0");

    private final String name;
    private final String iconUrl;
    private final String emote;

    CommandCategory(String search) {
        this.name = search;
        this.iconUrl = null;
        this.emote = null;
    }

    CommandCategory(String search, String url, String emote) {
        this.name = search;
        this.iconUrl = url;
        this.emote = emote;
    }

    public static @Nullable CommandCategory fromSearch(String input) {
        for (final CommandCategory value : values()) {
            if (input.equalsIgnoreCase(value.name()) || input.equalsIgnoreCase(value.getName())) {
                return value;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    @Nullable
    public String getEmote() {
        return emote;
    }

}