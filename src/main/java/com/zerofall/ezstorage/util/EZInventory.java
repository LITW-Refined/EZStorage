package com.zerofall.ezstorage.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.configuration.EZConfiguration;

public class EZInventory {

    private boolean hasChanges;
    public List<ItemStack> inventory;
    public long maxItems = 0;
    public String id;
    public boolean disabled;

    public EZInventory() {
        inventory = new ArrayList<ItemStack>();
    }

    public boolean getHasChanges() {
        return hasChanges;
    }

    public void setHasChanges() {
        hasChanges = true;
    }

    public void resetHasChanges() {
        hasChanges = false;
    }

    public ItemStack input(ItemStack itemStack) {
        // Inventory is full
        if (getTotalCount() >= maxItems) {
            return itemStack;
        }
        long space = maxItems - getTotalCount();
        // Only part of the stack can fit
        int amount = (int) Math.min(space, (long) itemStack.stackSize);
        ItemStack stack = mergeStack(itemStack, amount);
        setHasChanges();
        return stack;
    }

    public void sort() {
        Collections.sort(this.inventory, new ItemStackCountComparator());
        setHasChanges();
    }

    private ItemStack mergeStack(ItemStack itemStack, int amount) {
        boolean found = false;
        for (ItemStack group : inventory) {
            if (stacksEqual(group, itemStack)) {
                group.stackSize += amount;
                setHasChanges();
                found = true;
                break;
            }
        }

        // Add new group, if needed
        if (!found) {
            if (EZConfiguration.maxItemTypes != 0 && slotCount() > EZConfiguration.maxItemTypes) {
                return null;
            }
            ItemStack copy = itemStack.copy();
            copy.stackSize = amount;
            inventory.add(copy);
            setHasChanges();
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
        setHasChanges();
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
        setHasChanges();
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
                        setHasChanges();
                        return stack;
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public int getIndexOf(ItemStack itemStack) {
        int index = inventory.indexOf(itemStack);

        if (index == -1) {
            for (ItemStack inventoryStack : inventory) {
                index += 1;
                if (stacksEqual(itemStack, inventoryStack)) {
                    return index;
                }
            }
        }

        return index;
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
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        }
        if (stack1.getItemDamage() != stack2.getItemDamage()) {
            return false;
        }
        NBTTagCompound stack1Tag = stack1.getTagCompound();
        NBTTagCompound stack2Tag = stack2.getTagCompound();
        if (stack1Tag == null && stack2Tag == null) {
            return true;
        }
        if (stack1Tag == null || stack2Tag == null) {
            return false;
        }
        if (stack1Tag.equals(stack2Tag)) {
            return true;
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

    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.slotCount(); ++i) {
            ItemStack group = this.inventory.get(i);
            if (group != null && group.stackSize > 0) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                group.writeToNBT(nbttagcompound1);
                nbttagcompound1.setInteger("InternalCount", group.stackSize);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        tag.setTag("Internal", nbttaglist);
        tag.setLong("InternalMax", this.maxItems);
        tag.setBoolean("isDisabled", this.disabled);
    }

    public void readFromNBT(NBTTagCompound tag) {
        NBTTagList nbttaglist = tag.getTagList("Internal", 10);

        if (nbttaglist != null) {
            inventory = new ArrayList<ItemStack>();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                ItemStack stack = ItemStack.loadItemStackFromNBT(nbttagcompound1);
                if (nbttagcompound1.hasKey("InternalCount", 3)) {
                    stack.stackSize = (int) nbttagcompound1.getInteger("InternalCount");
                } else if (nbttagcompound1.hasKey("InternalCount", 4)) {
                    stack.stackSize = (int) nbttagcompound1.getLong("InternalCount");
                }
                this.inventory.add(stack);
            }
        }
        this.maxItems = tag.getLong("InternalMax");
        this.disabled = tag.getBoolean("isDisabled");
    }
}
