package com.zerofall.ezstorage.item;

import net.minecraft.item.Item;

import com.zerofall.ezstorage.EZStorage;

import cpw.mods.fml.common.registry.GameRegistry;

public class EZItem extends Item {

    public EZItem(String name) {
        registerItem(name, this);
    }

    public static void registerItem(String name, Item item) {
        item.setCreativeTab(EZStorage.instance.creativeTab);
        item.setUnlocalizedName(name);
        GameRegistry.registerItem(item, name);
    }
}
