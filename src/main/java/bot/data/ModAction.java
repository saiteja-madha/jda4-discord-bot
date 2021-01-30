package bot.data;

import org.jetbrains.annotations.Nullable;

public enum ModAction {

    BAN("Ban"),
    DEAFEN("Deafen"),
    KICK("Kick"),
    MUTE("Mute"),
    PURGE("Purge"),
    SETNICK("SetNick"),
    SOFTBAN("SoftBan"),
    TEMPBAN("TempBan"),
    TEMPMUTE("TempMute"),
    UNBAN("Unban"),
    UNDEAFEN("UnDeafen"),
    UNMUTE("Unmute"),
    VMUTE("Voice Mute"),
    VUNMUTE("Voice UnMute"),
    WARN("Warn");

    private final String search;

    ModAction(String text) {
        this.search = text;
    }

    public String getText() {
        return this.search;
    }

    @Nullable
    public static ModAction fromSearch(String text) {
        for (ModAction b : ModAction.values()) {
            if (b.search.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

}
