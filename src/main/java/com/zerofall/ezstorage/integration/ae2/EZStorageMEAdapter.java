package com.zerofall.ezstorage.integration.ae2;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.minecraft.item.ItemStack;

import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;
import com.zerofall.ezstorage.util.EZInventory;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;

public class EZStorageMEAdapter extends InventoryAdaptor {

    private final TileEntityInventoryProxy teInvProxy;

    public EZStorageMEAdapter(TileEntityInventoryProxy teInvProxy) {
        this.teInvProxy = teInvProxy;
    }

    @Override
    public Iterator<ItemSlot> iterator() {

        return new Iterator<ItemSlot>() {

            private int index = 0;
            private List<ItemStack> inventory = teInvProxy.getInventory() == null ? null
                : teInvProxy.getInventory().inventory;

            @Override
            public boolean hasNext() {
                if (inventory == null) {
                    return false;
                }
                return index < inventory.size() && inventory.get(index) != null;
            }

            @Override
            public ItemSlot next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                ItemStack itemStack = inventory.get(index);
                ItemSlot itemSlot = new ItemSlot();
                itemSlot.setItemStack(itemStack);
                itemSlot.setSlot(index);
                itemSlot.setExtractable(true);
                index++;
                return itemSlot;
            }
        };
    }

    @Override
    public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination) {
        EZInventory inventory = teInvProxy.getInventory();
        if (inventory == null) {
            return null;
        }
        int index = inventory.getIndexOf(filter);
        if (index == -1) {
            return null;
        }
        return inventory.getItemStackAt(index, amount);
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
        EZInventory inventory = teInvProxy.getInventory();
        if (inventory == null) {
            return null;
        }
        int index = inventory.getIndexOf(filter);
        if (index == -1) {
            return null;
        }
        return inventory.simulateRemove(index, amount);
    }

    @Override
    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
        IInventoryDestination destination) {
        return null;
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
        IInventoryDestination destination) {
        return null;
    }

    @Override
    public ItemStack addItems(ItemStack toBeAdded) {
        EZInventory inventory = teInvProxy.getInventory();
        if (inventory == null) {
            return toBeAdded;
        }
        return inventory.input(toBeAdded);
    }

    @Override
    public ItemStack simulateAdd(ItemStack toBeSimulated) {
        EZInventory inventory = teInvProxy.getInventory();
        if (inventory == null) {
            return toBeSimulated;
        }
        return inventory.simulateInput(toBeSimulated);
    }

    @Override
    public boolean containsItems() {
        EZInventory inventory = teInvProxy.getInventory();
        if (inventory == null) {
            return false;
        }
        for (ItemStack itemStack : inventory.inventory) {
            if (itemStack != null) {
                return true;
            }
        }
        return false;
    }
}
