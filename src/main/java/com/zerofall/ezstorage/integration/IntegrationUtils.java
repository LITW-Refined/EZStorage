package com.zerofall.ezstorage.integration;

import net.minecraft.entity.player.EntityPlayer;

import com.zerofall.ezstorage.integration.ae2.AE2Integration;
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

        if (ModIds.APPLIEDENERGISTICS2.isLoaded()) {
            AE2Integration.register();
        }
    }

    public static boolean isSpectatorMode(EntityPlayer player) {
        if (ModIds.ETFUTURUM.isLoaded()) {
            return EtFuturumUtils.isSpectatorMode(player);
        }
        return false;
    }
}
