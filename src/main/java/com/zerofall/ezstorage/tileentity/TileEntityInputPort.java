package com.zerofall.ezstorage.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;

import com.zerofall.ezstorage.util.BlockRef;

public class TileEntityInputPort extends TileEntity implements IUpdatePlayerListBox, ISidedInventory {

    private ItemStack[] inv = new ItemStack[1];
    public TileEntityStorageCore core;

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inv[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack != null) {
            if (stack.stackSize <= count) {
                setInventorySlotContents(index, null);
            } else {
                stack = stack.splitStack(count);
                if (stack.stackSize == 0) {
                    setInventorySlotContents(index, null);
                }
            }
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inv[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
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
        return true;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, int direction) {
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int direction) {
        return false;
    }

    @Override
    public void update() {
        if (this.core != null) {
            ItemStack stack = this.inv[0];
            if (stack != null && stack.stackSize > 0) {
                if (this.core.isPartOfMultiblock(new BlockRef(this))) {
                    this.inv[0] = this.core.input(stack);
                } else {
                    this.core = null;
                }
            }
        }
    }

    @Override
    public String getInventoryName() {
        return "input_port";
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return new int[] { 0 };
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
