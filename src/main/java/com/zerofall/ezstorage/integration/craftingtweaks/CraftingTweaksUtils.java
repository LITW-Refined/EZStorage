package com.zerofall.ezstorage.integration.craftingtweaks;

import java.util.List;

import net.blay09.mods.craftingtweaks.SimpleTweakProviderImpl;
import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;
import net.blay09.mods.craftingtweaks.api.SimpleTweakProvider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumFacing;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.gui.GuiStorageCore;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CraftingTweaksUtils {

    private static SimpleTweakProvider providerStorageCore;

    public static void init() {
        providerStorageCore = new SimpleTweakProviderImpl(Reference.MOD_ID);
        CraftingTweaksAPI.registerProvider(ContainerStorageCore.class, providerStorageCore);
    }

    @SideOnly(Side.CLIENT)
    public static void initGui(GuiStorageCore gui, List<GuiButton> buttons, int startIndex) {
        providerStorageCore.setGrid(startIndex, 9);
        providerStorageCore.setAlignToGrid(EnumFacing.WEST);
        providerStorageCore.initGui(gui, buttons);
    }
}
