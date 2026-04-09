package com.zerofall.ezstorage.nei;

import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.gui.GuiCraftingCore;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIEZStorageConfig implements IConfigureNEI {

    // Craft Slots starts at 44,114 in the gui, and NEI recipe panel starts at 25,6
    // the OffsetPositioner shifts recipe to match our slot positions
    private static final int CRAFTING_OFFSET_X = 19;
    private static final int CRAFTING_OFFSET_Y = 108;

    public void loadConfig() {
        API.registerGuiOverlayHandler(GuiCraftingCore.class, new NeiCraftingOverlay(), "crafting");
        if (EZConfiguration.neiCraftingGhostOverlay) {
            API.registerGuiOverlay(GuiCraftingCore.class, "crafting", CRAFTING_OFFSET_X, CRAFTING_OFFSET_Y);
        }
    }

    public String getName() {
        return "EZStorage";
    }

    public String getVersion() {
        return "${version}";
    }
}
