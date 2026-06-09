package com.zerofall.ezstorage.storage;

import java.util.List;

import net.minecraft.item.ItemStack;

import com.zerofall.ezstorage.util.EZInventory;

public class InternalStorageProvider implements IStorageProvider {

    private final EZInventory inventory;

    public InternalStorageProvider(EZInventory inventory) {
        this.inventory = inventory;
    }

    public EZInventory getInventory() {
        return inventory;
    }

    @Override
    public ItemStack input(ItemStack itemStack) {
        return inventory.input(itemStack);
    }

    @Override
    public ItemStack simulateInput(ItemStack itemStack) {
        return inventory.simulateInput(itemStack);
    }

    @Override
    public ItemStack extract(int index, int type) {
        return inventory.getItemsAt(index, type);
    }

    @Override
    public ItemStack extractAll(int index) {
        return inventory.extractAll(index);
    }

    @Override
    public ItemStack extractExact(int index, int amount) {
        return inventory.getItemStackAt(index, amount);
    }

    @Override
    public List<ItemStack> getAllItems() {
        return inventory.inventory;
    }

    @Override
    public long getTotalCount() {
        return inventory.getTotalCount();
    }

    @Override
    public long getCapacity() {
        return inventory.maxItems;
    }

    @Override
    public int getSlotCount() {
        return inventory.slotCount();
    }

    @Override
    public void markDirty() {
        inventory.setHasChanges();
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
