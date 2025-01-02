package com.zerofall.ezstorage.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

public class ContainerStorageCore extends Container {

    public TileEntityStorageCore tileEntity;

    public ContainerStorageCore(EntityPlayer player, World world, int x, int y, int z) {
        this.tileEntity = ((TileEntityStorageCore) world.getTileEntity(x, y, z));
        int startingY = 18;
        int startingX = 8;
        IInventory inventory = new InventoryBasic("title", false, this.rowCount() * 9);
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
            slotObject.putStack(this.tileEntity.inventory.input(stackInSlot));
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
        if (heldStack == null) {
            int type = 0;
            if (clickedButton == 1) {
                type = 1;
            }
            ItemStack stack = this.tileEntity.inventory.getItemsAt(itemIndex, type);
            if (stack == null) {
                return null;
            }
            // Shift click
            if (clickedButton == 0 && mode == 1) {
                if (!this.mergeItemStack(stack, this.rowCount() * 9, this.rowCount() * 9 + 36, true)) {
                    this.tileEntity.inventory.input(stack);
                }
            } else {
                playerIn.inventory.setItemStack(stack);
            }
            return stack;
        } else {
            playerIn.inventory.setItemStack(this.tileEntity.inventory.input(heldStack));
        }
        return null;
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
            this.tileEntity.sortInventory();
        }
    }
}
