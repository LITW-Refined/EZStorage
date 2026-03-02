package com.zerofall.ezstorage.util;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

public class ItemStackNameComparator implements Comparator<ItemStack> {

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        String n1 = o1.getDisplayName();
        String n2 = o2.getDisplayName();
        if (n1 == null) n1 = "";
        if (n2 == null) n2 = "";
        return n1.compareToIgnoreCase(n2);
    }
}
