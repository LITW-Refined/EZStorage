package com.zerofall.ezstorage.integration;

import cpw.mods.fml.common.Loader;

public enum ModIds {

    APPLIEDENERGISTICS2("appliedenergistics2"),
    CRAFTINGTWEAKS("craftingtweaks"),
    NEI("NotEnoughItems"),
    ETFUTURUM("etfuturum"),
    BAUBLES("Baubles"),
    BAUBLESEXPANDED("Baubles|Expanded"),
    HODGEPODGE("hodgepodge");

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
