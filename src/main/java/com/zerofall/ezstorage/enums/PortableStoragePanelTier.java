package com.zerofall.ezstorage.enums;

public enum PortableStoragePanelTier {

    TIER_1(0, 16, false),
    TIER_2(1, 32, false),
    TIER_3(2, 128, false),
    TIER_INFINITY(3, 0, true);

    public final int meta;
    public final int range;
    public final boolean isInfinity;

    private PortableStoragePanelTier(int meta, int range, boolean isInfinity) {
        this.meta = meta;
        this.range = range;
        this.isInfinity = isInfinity;
    }

    public static PortableStoragePanelTier getTierFromMeta(int meta) {
        PortableStoragePanelTier[] all = values();

        for (PortableStoragePanelTier tier : all) {
            if (tier.meta == meta) {
                return tier;
            }
        }

        return null;
    }

    public static PortableStoragePanelTier getNextTier(PortableStoragePanelTier tier) {
        return getTierFromMeta(tier.meta + 1);
    }

    public static PortableStoragePanelTier getPrevTier(PortableStoragePanelTier tier) {
        return getTierFromMeta(tier.meta - 1);
    }
}
