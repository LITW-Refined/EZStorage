package com.zerofall.ezstorage.integration;

import cpw.mods.fml.common.Loader;

public enum ModIds {

    CRAFTINGTWEAKS("craftingtweaks"),
    NEI("NotEnoughItems"),
    ETFUTURUM("etfuturum"),
    BAUBLES("Baubles"),
    BAUBLESEXPANDED("Baubles|Expanded");

    public final String modId;

    ModIds(String modId) {
        this.modId = modId;
    }

    public boolean isLoaded() {
        return Loader.isModLoaded(modId);
    }
}
