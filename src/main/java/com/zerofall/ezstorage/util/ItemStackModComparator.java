package com.zerofall.ezstorage.util;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameData;

public class ItemStackModComparator implements Comparator<ItemStack> {

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        String m1 = getModId(o1);
        String m2 = getModId(o2);
        return m1.compareToIgnoreCase(m2);
    }

    private String getModId(ItemStack stack) {
        String name = GameData.getItemRegistry()
            .getNameForObject(stack.getItem());
        if (name != null && name.contains(":")) {
            return name.split(":")[0];
        }
        return "minecraft";
    }
}
