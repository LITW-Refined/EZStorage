package com.zerofall.ezstorage.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.joml.Math;

public class EZInventory {

    public List<ItemGroup> inventory;
    public long maxItems = 0;

    public EZInventory() {
        inventory = new ArrayList<ItemGroup>();
    }

    public ItemStack input(ItemStack itemStack) {
        // Inventory is full
        if (getTotalCount() >= maxItems) {
            return itemStack;
        }
        long space = maxItems - getTotalCount();
        // Only part of the stack can fit
        int amount = (int) Math.min(space, (long) itemStack.stackSize);
        return mergeStack(itemStack, amount);
    }

    public void sort() {
        Collections.sort(this.inventory, new ItemGroup.CountComparator());
    }

    private ItemStack mergeStack(ItemStack itemStack, int amount) {
        for (ItemGroup group : inventory) {
            if (stacksEqual(group.itemStack, itemStack)) {
                group.count += amount;
                itemStack.stackSize -= amount;
                if (itemStack.stackSize <= 0) {
                    return null;
                } else {
                    return itemStack;
                }
            }
        }
        // Needs to add a space
        inventory.add(new ItemGroup(itemStack, amount));
        itemStack.stackSize -= amount;
        if (itemStack.stackSize <= 0) {
            return null;
        } else {
            return itemStack;
        }
    }

    // Type: 0= full stack, 1= half stack, 2= single
    public ItemStack getItemsAt(int index, int type) {
        if (index >= inventory.size()) {
            return null;
        }
        ItemGroup group = inventory.get(index);
        ItemStack stack = group.itemStack.copy();
        int size = (int) Math.min((long) stack.getMaxStackSize(), group.count);
        if (size > 1) {
            if (type == 1) {
                size = size / 2;
            } else if (type == 2) {
                size = 1;
            }
        }
        stack.stackSize = size;
        group.count -= size;
        if (group.count <= 0) {
            inventory.remove(index);
        }
        return stack;
    }

    public ItemStack getItemStackAt(int index, int size) {
        if (index >= inventory.size()) {
            return null;
        }
        ItemGroup group = inventory.get(index);
        ItemStack stack = group.itemStack.copy();
        if (size > group.count) {
            size = (int) Math.min(group.count, Integer.MAX_VALUE);
        }
        stack.stackSize = size;
        group.count -= size;
        if (group.count <= 0) {
            inventory.remove(index);
        }
        return stack;
    }

    public ItemStack getItems(ItemStack[] itemStacks) {
        for (ItemGroup group : inventory) {
            for (ItemStack itemStack : itemStacks) {
                if (stacksEqual(group.itemStack, itemStack)) {
                    if (group.count >= itemStack.stackSize) {
                        ItemStack stack = group.itemStack.copy();
                        stack.stackSize = itemStack.stackSize;
                        group.count -= itemStack.stackSize;
                        if (group.count <= 0) {
                            inventory.remove(group);
                        }
                        return stack;
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public int slotCount() {
        return inventory.size();
    }

    public static boolean stacksEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null && stack2 == null) {
            return true;
        }
        if (stack1 == null || stack2 == null) {
            return false;
        }
        if (stack1.getItem() == stack2.getItem()) {
            if (stack1.getItemDamage() == stack2.getItemDamage()) {
                if (stack1.getTagCompound() == stack2.getTagCompound()) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getTotalCount() {
        long count = 0;
        for (ItemGroup group : inventory) {
            count += group.count;
        }
        return count;
    }

    @Override
    public String toString() {
        return inventory.toString();
    }
}
