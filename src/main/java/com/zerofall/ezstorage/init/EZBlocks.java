package com.zerofall.ezstorage.init;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.zerofall.ezstorage.block.BlockCondensedStorage;
import com.zerofall.ezstorage.block.BlockCraftingBox;
import com.zerofall.ezstorage.block.BlockHyperStorage;
import com.zerofall.ezstorage.block.BlockInputPort;
import com.zerofall.ezstorage.block.BlockStorage;
import com.zerofall.ezstorage.block.BlockStorageCable;
import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.block.BlockStoragePanel;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

import cpw.mods.fml.common.registry.GameRegistry;

// spotless:off

public class EZBlocks {

    public static Block storage_core;
    public static Block storage_box;
    public static Block condensed_storage_box;
    public static Block hyper_storage_box;
    public static Block input_port;
    public static Block crafting_box;
    public static Block storage_panel;
    public static Block storage_cable;

    public static void init() {
        storage_core = new BlockStorageCore();
        storage_box = new BlockStorage();
        condensed_storage_box = new BlockCondensedStorage();
        hyper_storage_box = new BlockHyperStorage();
        input_port = new BlockInputPort();
        crafting_box = new BlockCraftingBox();
        storage_panel = new BlockStoragePanel();
        storage_cable = new BlockStorageCable();
    }

    public static void register() {
        GameRegistry.registerBlock(storage_core, storage_core.getUnlocalizedName().substring(5));
        GameRegistry.registerTileEntity(TileEntityStorageCore.class, "TileEntityStorageCore");
        GameRegistry.registerBlock(storage_box, storage_box.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(condensed_storage_box, condensed_storage_box.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(hyper_storage_box, hyper_storage_box.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(input_port, input_port.getUnlocalizedName().substring(5));
        GameRegistry.registerTileEntity(TileEntityInventoryProxy.class, "TileEntityInputPort");
        GameRegistry.registerBlock(crafting_box, crafting_box.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(storage_panel, storage_panel.getUnlocalizedName().substring(5));
        if (EZConfiguration.experimentalContent) {
            GameRegistry.registerBlock(storage_cable, storage_cable.getUnlocalizedName().substring(5));
        }
    }

    public static void registerRecipes() {

        String t2_1 = OreDictionary.getOres("blockBronze").size() != 0 ? "blockBronze" : "blockIron";
        String t2_2 = t2_1;

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(storage_core), "ABA", "BCB", "ABA", 'A', "logWood", 'B', "stickWood", 'C', Blocks.chest));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(storage_box), "ABA", "BCB", "ABA", 'A', "logWood", 'B', "plankWood", 'C', Blocks.chest));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(condensed_storage_box), "ACA", "EBE", "DCD", 'A', t2_1, 'B', storage_box, 'C', "ingotGold", 'D', t2_2, 'E', Blocks.chest));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(hyper_storage_box), "ABA", "ACA", "AAA", 'A', Blocks.obsidian, 'B', Items.nether_star, 'C', condensed_storage_box));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(input_port), " A ", " B ", " C ", 'A', Blocks.hopper, 'B', Blocks.piston, 'C', "blockQuartz"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crafting_box), " A ", " B ", " C ", 'A', Items.ender_pearl, 'B', Blocks.crafting_table, 'C', "gemDiamond"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(storage_panel), "ABA", "BCB", "ABA", 'A', "logWood", 'B', "stickWood", 'C', "plankWood"));
        
        if (EZConfiguration.experimentalContent) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(storage_cable, 16), "ABA", "BBB", "ABA", 'A', "logWood", 'B', "stickWood"));
        }

        if (OreDictionary.getOres("blockDarkSteel").size() != 0) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(hyper_storage_box), "ABA", "BCB", "ABA", 'A', "blockDarkSteel", 'B', Blocks.obsidian, 'C', condensed_storage_box));
        }

        if (OreDictionary.getOres("blockNetherite").size() != 0) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(hyper_storage_box), "ABA", "BCB", "ABA", 'A', "blockNetherite", 'B', Blocks.obsidian, 'C', condensed_storage_box));
        }
    }
}

// spotless:on
