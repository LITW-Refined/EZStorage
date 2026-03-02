package com.zerofall.ezstorage.enums;

public enum SortOrder {

    DESCENDING(0, "hud.msg.ezstorage.sort.order.descending"),
    ASCENDING(1, "hud.msg.ezstorage.sort.order.ascending");

    public final int index;
    public final String langKey;

    SortOrder(int index, String langKey) {
        this.index = index;
        this.langKey = langKey;
    }

    public static SortOrder fromIndex(int index) {
        for (SortOrder order : values()) {
            if (order.index == index) return order;
        }
        return DESCENDING;
    }

    public SortOrder next() {
        return fromIndex((this.index + 1) % values().length);
    }
}
