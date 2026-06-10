package com.zerofall.ezstorage.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.util.EZInventory;

public class TileEntityInventoryProxy extends TileEntityMultiblock implements ISidedInventory {

    private List<ItemStack> cachedItems;
    private int[] originalCounts;

    private void ensureCache() {
        if (cachedItems != null) return;
        TileEntityStorageCore core = getCore();
        List<ItemStack> source;
        if (core != null && core.getProviders()
            .size() > 1) {
            source = core.getUnifiedItemList();
        } else {
            EZInventory inventory = getInventory();
            source = inventory != null ? inventory.inventory : new ArrayList<ItemStack>();
        }
        cachedItems = new ArrayList<ItemStack>(source);
        originalCounts = new int[source.size()];
        for (int i = 0; i < source.size(); i++) {
            originalCounts[i] = source.get(i).stackSize;
        }
    }

    private void invalidateCache() {
        cachedItems = null;
        originalCounts = null;
    }

    private boolean hasExternalProviders() {
        TileEntityStorageCore core = getCore();
        return core != null && core.getProviders()
            .size() > 1;
    }

    @Override
    public int getSizeInventory() {
        ensureCache();
        int size = cachedItems.size();
        if (hasExternalProviders()) {
            TileEntityStorageCore core = getCore();
            if (core.getUnifiedCapacity() > core.getUnifiedTotalCount()) {
                size += 1;
            }
        } else {
            EZInventory inventory = getInventory();
            if (inventory != null && inventory.getTotalCount() < inventory.maxItems
                && (EZConfiguration.maxItemTypes == 0 || inventory.slotCount() < EZConfiguration.maxItemTypes)) {
                size += 1;
            }
        }
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        ensureCache();
        if (index >= 0 && index < cachedItems.size()) {
            return cachedItems.get(index);
        }
        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        TileEntityStorageCore core = getCore();
        if (core == null) return null;

        ensureCache();
        if (index < 0 || index >= cachedItems.size()) return null;

        ItemStack result;
        if (hasExternalProviders()) {
            result = core.unifiedExtractExact(index, count);
        } else {
            EZInventory inventory = getInventory();
            if (inventory == null) return null;
            result = inventory.getItemStackAt(index, count);
        }
        if (result != null) {
            invalidateCache();
            core.updateTileEntity();
        }
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        TileEntityStorageCore core = getCore();
        if (core == null) return;

        if (stack == null || stack.stackSize == 0) {
            invalidateCache();
            core.updateTileEntity();
            return;
        }

        if (hasExternalProviders()) {
            ensureCache();
            int previousCount = 0;
            if (index < cachedItems.size()) {
                ItemStack previous = cachedItems.get(index);
                if (previous != null && EZInventory.stacksEqual(previous, stack)) {
                    previousCount = originalCounts[index];
                }
            }
            int newCount = stack.stackSize - previousCount;

            if (newCount > 0) {
                ItemStack toInput = stack.copy();
                toInput.stackSize = newCount;
                core.unifiedInput(toInput);
            }
        } else {
            EZInventory inventory = getInventory();
            if (inventory != null) {
                inventory.input(stack);
            }
        }

        invalidateCache();
        core.updateTileEntity();
    }

    @Override
    public void markDirty() {
        if (cachedItems != null && originalCounts != null && hasExternalProviders()) {
            TileEntityStorageCore core = getCore();
            if (core != null) {
                for (int i = 0; i < cachedItems.size() && i < originalCounts.length; i++) {
                    ItemStack cached = cachedItems.get(i);
                    int diff = cached.stackSize - originalCounts[i];
                    if (diff > 0) {
                        ItemStack toInput = cached.copy();
                        toInput.stackSize = diff;
                        core.unifiedInput(toInput);
                    } else if (diff < 0) {
                        core.unifiedExtractExact(i, -diff);
                    }
                }
                invalidateCache();
                core.updateTileEntity();
            }
        }
        super.markDirty();
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
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack != null;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int direction) {
        ensureCache();
        if (index >= cachedItems.size()) return false;
        ItemStack slot = cachedItems.get(index);
        return slot != null && EZInventory.stacksEqual(slot, stack);
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
        return null;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }
}
