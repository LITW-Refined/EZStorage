package com.zerofall.ezstorage.integration.etfuturum;

import net.minecraft.entity.player.EntityPlayer;

import ganymedes01.etfuturum.spectator.SpectatorMode;

public class EtFuturumUtils {

    public static boolean isSpectatorMode(EntityPlayer player) {
        return SpectatorMode.isSpectator(player);
    }
}
