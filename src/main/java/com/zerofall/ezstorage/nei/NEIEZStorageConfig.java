package com.zerofall.ezstorage.nei;

import com.zerofall.ezstorage.gui.GuiCraftingCore;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIEZStorageConfig implements IConfigureNEI {

    public void loadConfig() {
        API.registerGuiOverlayHandler(GuiCraftingCore.class, new NeiCraftingOverlay(), "crafting");
    }

    public String getName() {
        return "EZStorage";
    }

    public String getVersion() {
        return "${version}";
    }
}
