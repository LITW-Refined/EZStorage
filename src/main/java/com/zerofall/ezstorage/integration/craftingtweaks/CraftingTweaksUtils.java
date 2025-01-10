package com.zerofall.ezstorage.integration.craftingtweaks;

import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;
import net.blay09.mods.craftingtweaks.api.SimpleTweakProvider;
import net.minecraft.util.EnumFacing;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CraftingTweaksUtils {

    private static SimpleTweakProvider providerStorageCoreCrafting;

    public static void init() {}

    @SideOnly(Side.CLIENT)
    public static void registerStorageCoreCrafting(int startIndex) {
        // This registers a new provider and let everything manage CraftingTweaks.
        // The laternative would be creating an own instance of SimpleTweaksProviderImpl on each new GuiCraftingCore
        // instance and handle the buttons on the gui itself via initGui(). However, as the startIndex should always be
        // the same shared for all gui instances, we can use only one shared provider instance.
        if (providerStorageCoreCrafting == null) {
            providerStorageCoreCrafting = CraftingTweaksAPI
                .registerSimpleProvider(Reference.MOD_ID, ContainerStorageCoreCrafting.class);
            providerStorageCoreCrafting.setGrid(startIndex, 9);
            providerStorageCoreCrafting.setAlignToGrid(EnumFacing.WEST);
        }
    }
}
