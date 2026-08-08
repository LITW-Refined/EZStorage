package com.zerofall.ezstorage.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.Test;

import com.zerofall.ezstorage.util.EZInventory;

public class ContainerStorageCoreCraftingTest {

    @Test
    public void refillUsesStorageBeforePlayerForConsumableIngredients() {
        Item consumable = new Item();
        EZInventory storage = storageWith(new ItemStack(consumable));
        InventoryPlayer playerInventory = playerWith(new ItemStack(consumable));

        ContainerStorageCoreCrafting.MatchingIngredient result = ContainerStorageCoreCrafting
            .getMatchingItemForCraftingRefill(new ItemStack(consumable), storage, playerInventory);

        assertNotNull(result);
        assertTrue(result.fromStorage);
        assertEquals(0, storage.inventory.size());
        assertNotNull(playerInventory.mainInventory[0]);
    }

    @Test
    public void refillReusesPlayerToolBeforeStorageCopy() {
        Item tool = new Item();
        tool.setContainerItem(tool);
        EZInventory storage = storageWith(new ItemStack(tool));
        InventoryPlayer playerInventory = playerWith(new ItemStack(tool));

        ContainerStorageCoreCrafting.MatchingIngredient result = ContainerStorageCoreCrafting
            .getMatchingItemForCraftingRefill(new ItemStack(tool), storage, playerInventory);

        assertNotNull(result);
        assertFalse(result.fromStorage);
        assertEquals(1, storage.inventory.get(0).stackSize);
        assertNull(playerInventory.mainInventory[0]);
    }

    private static EZInventory storageWith(ItemStack stack) {
        EZInventory storage = new EZInventory();
        storage.inventory.add(stack);
        return storage;
    }

    private static InventoryPlayer playerWith(ItemStack stack) {
        InventoryPlayer playerInventory = new InventoryPlayer(null);
        playerInventory.mainInventory[0] = stack;
        return playerInventory;
    }
}
