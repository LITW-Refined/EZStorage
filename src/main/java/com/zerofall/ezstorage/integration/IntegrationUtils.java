package com.zerofall.ezstorage.integration;

import net.minecraft.entity.player.EntityPlayer;

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
}
