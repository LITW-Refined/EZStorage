package com.zerofall.ezstorage.item;

import net.minecraft.item.Item;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;

public class EZItem extends Item {

    public EZItem(String name) {
        setCreativeTab(EZStorage.instance.creativeTab);
        setUnlocalizedName(name);
        setTextureName(Reference.MOD_ID + ":" + name);
    }
}
