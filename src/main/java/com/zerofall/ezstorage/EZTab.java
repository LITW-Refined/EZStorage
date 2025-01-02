package com.zerofall.ezstorage;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import com.zerofall.ezstorage.init.EZBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EZTab extends CreativeTabs {

    public EZTab() {
        super("EZStorage");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return Item.getItemFromBlock(EZBlocks.condensed_storage_box);
    }
}
