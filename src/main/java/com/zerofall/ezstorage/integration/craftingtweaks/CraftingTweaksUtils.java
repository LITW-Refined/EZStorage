package com.zerofall.ezstorage.integration.craftingtweaks;

import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;
import net.blay09.mods.craftingtweaks.api.SimpleTweakProvider;
import net.minecraft.util.EnumFacing;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;

public class CraftingTweaksUtils {

    private static SimpleTweakProvider providerStorageCoreCrafting;

    public static void init() {
        // This registers a new provider and let everything manage CraftingTweaks.
        // The laternative would be creating an own instance of SimpleTweaksProviderImpl on each new GuiCraftingCore
        // instance and handle the buttons on the gui itself via initGui(). However, as the startIndex should always be
        // the same shared for all gui instances, we can use only one shared provider instance.
        final int startIndex = 82; // 54 (ezinventory) + 27 (player inventory) + 1 (crafting result) = 82
        if (providerStorageCoreCrafting == null) {
            providerStorageCoreCrafting = CraftingTweaksAPI
                .registerSimpleProvider(Reference.MOD_ID, ContainerStorageCoreCrafting.class);
            providerStorageCoreCrafting.setGrid(startIndex, 9);
            providerStorageCoreCrafting.setAlignToGrid(EnumFacing.WEST);
        }
    }
}
