package com.zerofall.ezstorage.container;

import java.time.LocalDateTime;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

public class ContainerStorageCore extends Container {

    public EZInventory inventory = new EZInventory();
    public LocalDateTime inventoryUpdateTimestamp = LocalDateTime.now();

    public ContainerStorageCore(EntityPlayer player, EZInventory inventory) {
        this(player);
        this.inventory = inventory;
    }

    public ContainerStorageCore(EntityPlayer player) {
        int startingY = 18;
        int startingX = 8;
        IInventory inventory = new InventoryBasic("Storage Core", false, this.rowCount() * 9);
        for (int i = 0; i < this.rowCount(); i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(inventory, j + i * 9, startingX + j * 18, startingY + i * 18));
            }
        }

        bindPlayerInventory(player.inventory);
    }

    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(
                    new Slot(
                        inventoryPlayer,
                        (j + i * 9) + 9,
                        playerInventoryX() + j * 18,
                        playerInventoryY() + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i, playerInventoryX() + i * 18, playerInventoryY() + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    // Shift clicking
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slotObject = (Slot) inventorySlots.get(index);
        if (slotObject != null && slotObject.getHasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            slotObject.putStack(this.inventory.input(stackInSlot));
            EZInventoryManager.sendToClients(inventory);
        }
        return null;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        if (slotId < this.rowCount() * 9 && slotId >= 0) {
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    public ItemStack customSlotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        int itemIndex = slotId;
        ItemStack heldStack = playerIn.inventory.getItemStack();
        ItemStack result = null;
        boolean sendToClients = false;

        if (heldStack == null) {
            int type = 0;
            if (clickedButton == 1) {
                type = 1;
            }
            ItemStack stack = this.inventory.getItemsAt(itemIndex, type);
            if (stack != null) {
                // Shift click
                if (clickedButton == 0 && mode == 1) {
                    if (!this.mergeItemStack(stack, this.rowCount() * 9, this.rowCount() * 9 + 36, true)) {
                        this.inventory.input(stack);
                    }
                } else {
                    playerIn.inventory.setItemStack(stack);
                }
                sendToClients = true;
                result = stack;
            }
        } else {
            playerIn.inventory.setItemStack(this.inventory.input(heldStack));
            sendToClients = true;
        }

        if (sendToClients) {
            EZInventoryManager.sendToClients(inventory);
        }

        return result;
    }

    protected int playerInventoryX() {
        return 8;
    }

    protected int playerInventoryY() {
        return 140;
    }

    protected int rowCount() {
        return 6;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.worldObj.isRemote) {
            this.inventory.sort();
            EZInventoryManager.sendToClients(inventory);
        }
    }
}
