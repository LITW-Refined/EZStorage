package com.zerofall.ezstorage.integration;

import net.minecraft.entity.player.EntityPlayer;

import com.zerofall.ezstorage.integration.etfuturum.EtFuturumUtils;

public class IntegrationUtils {

    public static boolean isSpectatorMode(EntityPlayer player) {
        if (ModIds.ETFUTURUM.isLoaded()) {
            return EtFuturumUtils.isSpectatorMode(player);
        }
        return false;
    }
}
