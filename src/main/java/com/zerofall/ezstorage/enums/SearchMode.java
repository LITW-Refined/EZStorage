package com.zerofall.ezstorage.enums;

public enum SearchMode {

    AUTO(0, "hud.msg.ezstorage.search.mode.auto"),
    NEI_SYNC(1, "hud.msg.ezstorage.search.mode.nei_sync"),
    NEI_STANDARD(2, "hud.msg.ezstorage.search.mode.nei_standard"),
    STANDARD(3, "hud.msg.ezstorage.search.mode.standard");

    public final int index;
    public final String langKey;

    SearchMode(int index, String langKey) {
        this.index = index;
        this.langKey = langKey;
    }

    public static SearchMode fromIndex(int index) {
        for (SearchMode mode : values()) {
            if (mode.index == index) return mode;
        }
        return AUTO;
    }

    public SearchMode next() {
        return fromIndex((this.index + 1) % values().length);
    }
}
