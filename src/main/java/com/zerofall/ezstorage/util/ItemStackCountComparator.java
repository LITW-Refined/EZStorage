package com.zerofall.ezstorage.util;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

public class ItemStackCountComparator implements Comparator<ItemStack> {

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        Integer l1 = o1.stackSize;
        Integer l2 = o2.stackSize;
        return l2.compareTo(l1);
    }
}
