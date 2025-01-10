package com.zerofall.ezstorage.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.joml.Math;

import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.ItemGroup;

public class TileEntityInventoryProxy extends TileEntity implements ISidedInventory {

    public TileEntityStorageCore core;

    @Override
    public int getSizeInventory() {
        if (core == null) {
            return 0;
        }
        int size = core.inventory.inventory.size();
        if (core.inventory.getTotalCount() < core.inventory.maxItems && core.inventory.slotCount() < 1000) {
            size += 1;
        }
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index < core.inventory.inventory.size()) {
            ItemGroup group = core.inventory.inventory.get(index);
            ItemStack copy = group.itemStack.copy();
            copy.stackSize = (int) group.count;
            return copy;
        }
        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = core.inventory.getItemStackAt(index, count);
        core.updateTileEntity();
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (stack != null && stack.stackSize != 0 && isItemValidForSlot(index, stack)) {
            core.inventory.input(stack);
            core.updateTileEntity();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return (int) Math.min(core.inventory.maxItems, Integer.MAX_VALUE);
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
        if (core == null) {
            return false;
        }

        int foundIndex = -1;
        int itemsCount = core.inventory.inventory.size();

        // Search for existing group of the given item type
        for (int i = 0; i < itemsCount; i++) {
            ItemGroup group = core.inventory.inventory.get(i);
            if (EZInventory.stacksEqual(group.itemStack, stack)) {
                foundIndex = i;
            }
        }

        // Permit if the destination is a new slot and the item doesn't exist
        if (index >= core.inventory.inventory.size()) {
            return foundIndex == -1;
        }

        // Permit if the item eixsts and is in the destination slot
        if (foundIndex == index) {
            return true;
        }

        // If the item doesn't exist, permit if the destination slot is empty
        if (index == -1) {
            ItemGroup group = core.inventory.inventory.get(index);
            if (group == null || group.count == 0 || group.itemStack == null) {
                return true;
            }
        }

        // Deny if the item exists but in another slot
        return false;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int direction) {
        if (core == null) {
            return false;
        }

        // Check if the slot is empty and if the item does not exist yet
        if (index >= core.inventory.inventory.size()) {
            return false;
        }

        // The item in the slot needs to be the same as the given item
        ItemGroup theGroup = core.inventory.inventory.get(index);
        return theGroup != null && theGroup.itemStack != null && EZInventory.stacksEqual(theGroup.itemStack, stack);
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
        }
        return stack;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }
}
