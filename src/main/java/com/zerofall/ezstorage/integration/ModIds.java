package com.zerofall.ezstorage.integration;

import cpw.mods.fml.common.Loader;

public enum ModIds {

    NEI("NotEnoughItems"),
    ETFUTURUM("etfuturum");

    public final String modId;

    ModIds(String modId) {
        this.modId = modId;
    }

    public boolean isLoaded() {
        return Loader.isModLoaded(modId);
    }
}
