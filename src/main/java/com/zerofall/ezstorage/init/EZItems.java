package com.zerofall.ezstorage.init;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.zerofall.ezstorage.item.ItemPortableStoragePanel;
import com.zerofall.ezstorage.recipes.PortableStoragePanelUpgradeRecipe;

import cpw.mods.fml.common.registry.GameRegistry;

// spotless:off

public class EZItems {

    public static Item portable_storage_panel;

    public static void init() {
        portable_storage_panel = new ItemPortableStoragePanel();
    }

    public static void register() {
        GameRegistry.registerItem(
            portable_storage_panel,
            portable_storage_panel.getUnlocalizedName()
                .substring(5));
    }

    public static void registerRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(portable_storage_panel), "ABA", "BCB", "DBD", 'A', Blocks.redstone_torch, 'B', "slabWood", 'C', EZBlocks.storage_panel, 'D', "ingotGold"));
        GameRegistry.addRecipe(new PortableStoragePanelUpgradeRecipe());
    }
}

// spotless:on
