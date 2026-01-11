package com.zerofall.ezstorage.integration;

import cpw.mods.fml.common.Loader;

public enum ModIds {

    CRAFTINGTWEAKS("craftingtweaks"),
    NEI("NotEnoughItems"),
    ETFUTURUM("etfuturum"),
    BAUBLES("Baubles"),
    BAUBLESEXPANDED("Baubles|Expanded"),
    HODGEPODGE("lawoeju2398");

    public final String modId;
    private boolean loaded;
    private boolean loadedCached;

    ModIds(String modId) {
        this.modId = modId;
    }

    public boolean isLoaded() {
        if (!loadedCached) {
            loadedCached = true;
            loaded = Loader.isModLoaded(modId);
        }
        return loaded;
    }
}
