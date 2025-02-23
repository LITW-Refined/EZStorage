package com.zerofall.ezstorage.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.util.EZInventory;

public class TileEntityInventoryProxy extends TileEntity implements ISidedInventory {

    public TileEntityStorageCore core;

    public EZInventory getInventory() {
        if (core != null) {
            return core.getInventory();
        }
        return null;
    }

    @Override
    public int getSizeInventory() {
        EZInventory inventory = getInventory();
        if (inventory == null) {
            return 1;
        }
        int size = inventory.inventory.size();
        if (inventory.getTotalCount() < inventory.maxItems
            && (EZConfiguration.maxItemTypes == 0 || inventory.slotCount() < EZConfiguration.maxItemTypes)) {
            size += 1;
        }
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        EZInventory inventory = getInventory();
        if (inventory != null && index < inventory.inventory.size()) {
            return inventory.inventory.get(index);
        }
        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        EZInventory inventory = getInventory();
        if (inventory == null) {
            return null;
        }
        ItemStack result = inventory.getItemStackAt(index, count);
        core.updateTileEntity();
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        EZInventory inventory = getInventory();
        if (inventory == null) {
            return;
        } else if (stack == null || stack.stackSize == 0) {
            inventory.inventory.remove(index);
        } else if (index >= inventory.inventory.size()) {
            inventory.input(stack);
        } else if (isItemValidForSlot(index, stack)) {
            inventory.inventory.set(index, stack);
        } else {
            return;
        }
        core.updateTileEntity();
    }

    @Override
    public int getInventoryStackLimit() {
        EZInventory inventory = getInventory();
        if (inventory == null) {
            return 0;
        }
        return (int) Math.min(inventory.maxItems, Integer.MAX_VALUE);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {
        // Nothing todo here
    }

    @Override
    public void closeInventory() {
        // Nothing todo here
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        EZInventory inventory = getInventory();
        if (inventory == null) {
            return false;
        }

        int foundIndex = -1;
        int itemsCount = inventory.inventory.size();

        // Search for existing group of the given item type
        for (int i = 0; i < itemsCount; i++) {
            ItemStack group = inventory.inventory.get(i);
            if (EZInventory.stacksEqual(group, stack)) {
                foundIndex = i;
            }
        }

        // Permit if the destination is a new slot and the item doesn't exist
        if (index >= inventory.inventory.size()) {
            return true; // return foundIndex == -1;
        }

        // Permit if the item eixsts and is in the destination slot
        if (foundIndex == index) {
            return true;
        }

        // If the item doesn't exist, permit if the destination slot is empty
        if (index == -1) {
            ItemStack group = inventory.inventory.get(index);
            if (group == null || group.stackSize == 0) {
                return true;
            }
        }

        // Permit if the item exists but in another slot
        return false;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int direction) {
        EZInventory inventory = getInventory();
        if (inventory == null) {
            return false;
        }

        // Check if the slot is empty and if the item does not exist yet
        if (index >= inventory.inventory.size()) {
            return false;
        }

        // The item in the slot needs to be the same as the given item
        ItemStack theGroup = inventory.inventory.get(index);
        return theGroup != null && EZInventory.stacksEqual(theGroup, stack);
    }

    @Override
    public String getInventoryName() {
        return "proxy_port";
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        int size = getSizeInventory();
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        ItemStack stack = getStackInSlot(index);
        if (stack != null) {
            setInventorySlotContents(index, null);
            core.updateTileEntity();
        }
        return stack;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }
}
