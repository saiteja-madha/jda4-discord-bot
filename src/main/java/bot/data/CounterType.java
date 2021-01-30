package bot.data;

import org.jetbrains.annotations.Nullable;

public enum CounterType {
    MEMBERS("Members"),
    BOTS("Bots"),
    ALL("All");

    private final String search;

    CounterType(String text) {
        this.search = text;
    }

    @Nullable
    public static CounterType fromSearch(String text) {
        for (CounterType b : CounterType.values()) {
            if (b.search.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public String getText() {
        return this.search;
    }

}
