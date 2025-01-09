package com.zerofall.ezstorage.integration;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import com.zerofall.ezstorage.gui.GuiStorageCore;
import com.zerofall.ezstorage.integration.craftingtweaks.CraftingTweaksUtils;
import com.zerofall.ezstorage.integration.etfuturum.EtFuturumUtils;
import com.zerofall.ezstorage.nei.NeiHandler;

public class IntegrationUtils {

    public static void init() {
        if (ModIds.CRAFTINGTWEAKS.isLoaded()) {
            CraftingTweaksUtils.init();
        }
    }

    public static void initClient() {
        if (ModIds.NEI.isLoaded()) {
            NeiHandler.init();
        }
    }

    public static boolean isSpectatorMode(EntityPlayer player) {
        if (ModIds.ETFUTURUM.isLoaded()) {
            return EtFuturumUtils.isSpectatorMode(player);
        }
        return false;
    }

    public static void applyCraftingTweaks(GuiScreen gui, List<GuiButton> buttons, int startIndex) {
        if (ModIds.CRAFTINGTWEAKS.isLoaded() && gui instanceof GuiStorageCore guiStorageCore) {
            CraftingTweaksUtils.initGui(guiStorageCore, buttons, startIndex);
        }
    }
}
