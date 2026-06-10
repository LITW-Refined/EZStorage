package com.zerofall.ezstorage.util;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameData;

public class ItemStackCountComparator implements Comparator<ItemStack> {

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        int result = Integer.compare(o2.stackSize, o1.stackSize);
        if (result != 0) return result;
        return tiebreak(o1, o2);
    }

    static int tiebreak(ItemStack o1, ItemStack o2) {
        String n1 = o1.getDisplayName();
        String n2 = o2.getDisplayName();
        if (n1 == null) n1 = "";
        if (n2 == null) n2 = "";
        int result = n1.compareToIgnoreCase(n2);
        if (result != 0) return result;
        String r1 = GameData.getItemRegistry().getNameForObject(o1.getItem());
        String r2 = GameData.getItemRegistry().getNameForObject(o2.getItem());
        if (r1 == null) r1 = "";
        if (r2 == null) r2 = "";
        return r1.compareTo(r2);
    }
}
