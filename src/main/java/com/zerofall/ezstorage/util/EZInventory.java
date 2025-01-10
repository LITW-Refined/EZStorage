package com.zerofall.ezstorage.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.joml.Math;

import com.zerofall.ezstorage.configuration.EZConfiguration;

public class EZInventory {

    public List<ItemStack> inventory;
    public long maxItems = 0;

    public EZInventory() {
        inventory = new ArrayList<ItemStack>();
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
        Collections.sort(this.inventory, new ItemStackCountComparator());
    }

    private ItemStack mergeStack(ItemStack itemStack, int amount) {
        boolean found = false;
        for (ItemStack group : inventory) {
            if (stacksEqual(group, itemStack)) {
                group.stackSize += amount;
                found = true;
                break;
            }
        }

        // Add new group, if needed
        if (!found) {
            if (slotCount() > EZConfiguration.maxItemTypes) {
                return null;
            }
            ItemStack copy = itemStack.copy();
            copy.stackSize = amount;
            inventory.add(copy);
        }

        // Adjust input/return stack
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
        ItemStack group = inventory.get(index);
        ItemStack stack = group.copy();
        int size = Math.min(stack.getMaxStackSize(), group.stackSize);
        if (size > 1) {
            if (type == 1) {
                size = size / 2;
            } else if (type == 2) {
                size = 1;
            }
        }
        stack.stackSize = size;
        group.stackSize -= size;
        if (group.stackSize <= 0) {
            inventory.remove(index);
        }
        return stack;
    }

    public ItemStack getItemStackAt(int index, int size) {
        if (index >= inventory.size()) {
            return null;
        }
        ItemStack group = inventory.get(index);
        ItemStack stack = group.copy();
        if (size > group.stackSize) {
            size = group.stackSize;
        }
        stack.stackSize = size;
        group.stackSize -= size;
        if (group.stackSize <= 0) {
            inventory.remove(index);
        }
        return stack;
    }

    public ItemStack getItems(ItemStack[] itemStacks) {
        for (ItemStack group : inventory) {
            for (ItemStack itemStack : itemStacks) {
                if (stacksEqual(group, itemStack)) {
                    if (group.stackSize >= itemStack.stackSize) {
                        ItemStack stack = group.copy();
                        stack.stackSize = itemStack.stackSize;
                        group.stackSize -= itemStack.stackSize;
                        if (group.stackSize <= 0) {
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
        for (ItemStack group : inventory) {
            count += group.stackSize;
        }
        return count;
    }

    @Override
    public String toString() {
        return inventory.toString();
    }
}
