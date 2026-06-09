package com.zerofall.ezstorage.storage;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IStorageProvider {

    /** Store items, return remainder (or null if fully stored) */
    ItemStack input(ItemStack itemStack);

    /** Simulate storing items without modifying state */
    ItemStack simulateInput(ItemStack itemStack);

    /** Extract by index: type 0=full stack, 1=half stack, 2=one item */
    ItemStack extract(int index, int type);

    /** Extract all items at the given index */
    ItemStack extractAll(int index);

    /** Extract up to the specified amount from the given index */
    ItemStack extractExact(int index, int amount);

    /** Get all items (merged view) */
    List<ItemStack> getAllItems();

    /** Total item count across all slots */
    long getTotalCount();

    /** Maximum capacity */
    long getCapacity();

    /** Number of distinct item types */
    int getSlotCount();

    /** Notify this provider that its contents changed */
    void markDirty();

    /** Check if this provider is still valid (external container may be destroyed) */
    boolean isValid();
}
