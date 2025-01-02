package com.zerofall.ezstorage.util;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

public class ItemGroup {

    public ItemStack itemStack;
    public long count;

    public ItemGroup(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.count = itemStack.stackSize;
    }

    public ItemGroup(ItemStack itemStack, long count) {
        this.itemStack = itemStack;
        this.count = count;
    }

    @Override
    public String toString() {
        return itemStack.getDisplayName() + ":" + count;
    }

    public static class CountComparator implements Comparator<ItemGroup> {

        @Override
        public int compare(ItemGroup o1, ItemGroup o2) {
            Long l1 = (Long) o1.count;
            Long l2 = (Long) o2.count;
            return l2.compareTo(l1);
        }

    }
}
