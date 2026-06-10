package com.zerofall.ezstorage.container;

import java.time.LocalDateTime;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;

import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

public class ContainerStorageCore extends Container {

    public EZInventory inventory = new EZInventory();
    public TileEntityStorageCore coreTileEntity;
    public LocalDateTime inventoryUpdateTimestamp = LocalDateTime.now();

    public ContainerStorageCore(EntityPlayer player, EZInventory inventory) {
        this(player);
        this.inventory = inventory;
    }

    public ContainerStorageCore(EntityPlayer player, EZInventory inventory, TileEntityStorageCore coreTileEntity) {
        this(player, inventory);
        this.coreTileEntity = coreTileEntity;
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
            if (coreTileEntity != null) {
                ItemStack stackInSlot = slotObject.getStack();
                slotObject.putStack(coreTileEntity.unifiedInput(stackInSlot));
            }
            EZInventoryManager.sendToClients(inventory, coreTileEntity);
            forceSyncPlayerState(playerIn);
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
            // mode=2: space key → extract all and merge into player inventory
            if (mode == 2) {
                ItemStack all;
                if (coreTileEntity != null) {
                    all = coreTileEntity.unifiedExtract(itemIndex, 0);
                } else {
                    all = this.inventory.extractAll(itemIndex);
                }
                if (all != null) {
                    this.mergeItemStack(all, this.rowCount() * 9, this.rowCount() * 9 + 36, true);
                    if (all.stackSize > 0) {
                        if (coreTileEntity != null) {
                            coreTileEntity.unifiedInput(all);
                        } else {
                            this.inventory.input(all);
                        }
                    }
                    sendToClients = true;
                }
                return null;
            }
            int type = 0;
            if (clickedButton == 1) {
                type = 1;
            }
            ItemStack stack;
            if (coreTileEntity != null) {
                stack = coreTileEntity.unifiedExtract(itemIndex, type);
            } else {
                stack = this.inventory.getItemsAt(itemIndex, type);
            }
            if (stack != null) {
                // Shift click
                if (clickedButton == 0 && mode == 1) {
                    if (!this.mergeItemStack(stack, this.rowCount() * 9, this.rowCount() * 9 + 36, true)) {
                        if (coreTileEntity != null) {
                            coreTileEntity.unifiedInput(stack);
                        } else {
                            this.inventory.input(stack);
                        }
                    }
                } else {
                    playerIn.inventory.setItemStack(stack);
                }
                sendToClients = true;
                result = stack;
            }
        } else {
            if (coreTileEntity != null) {
                ItemStack remainder = coreTileEntity.unifiedInput(heldStack);
                playerIn.inventory.setItemStack(remainder);
            }
            sendToClients = true;
        }

        if (sendToClients) {
            EZInventoryManager.sendToClients(inventory, coreTileEntity);
            forceSyncPlayerState(playerIn);
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

    /**
     * Force-sync the entire player container state (all slots + cursor) to the
     * client. Needed because client-side prediction on a synthetic merged
     * inventory view may diverge from the server's actual insertion result when
     * external storage is involved. detectAndSendChanges alone is insufficient
     * because the server may put the original stack back unchanged.
     */
    protected void forceSyncPlayerState(EntityPlayer player) {
        if (player instanceof EntityPlayerMP playerMP) {
            playerMP.playerNetServerHandler.sendPacket(
                new S30PacketWindowItems(playerMP.openContainer.windowId, playerMP.openContainer.getInventory()));
            playerMP.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, player.inventory.getItemStack()));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.worldObj.isRemote) {
            this.inventory.sort();
            EZInventoryManager.sendToClients(inventory, coreTileEntity);
        }
    }

    public void importPlayerInventory(EntityPlayer player, boolean hotbarOnly) {
        InventoryPlayer inv = player.inventory;
        int start = hotbarOnly ? 0 : 9;
        int end = hotbarOnly ? 9 : 36;
        for (int i = start; i < end; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null) {
                ItemStack remainder;
                if (coreTileEntity != null) {
                    remainder = coreTileEntity.unifiedInput(stack);
                } else {
                    remainder = this.inventory.input(stack);
                }
                inv.setInventorySlotContents(i, remainder);
            }
        }
        EZInventoryManager.sendToClients(inventory, coreTileEntity);
    }

    public void dropItem(int itemIndex, int amount, EntityPlayer player) {
        if (itemIndex >= this.inventory.slotCount()) {
            return;
        }
        ItemStack toDrop;
        if (amount <= 0) {
            if (coreTileEntity != null) {
                toDrop = coreTileEntity.unifiedExtract(itemIndex, 0);
            } else {
                toDrop = this.inventory.extractAll(itemIndex);
            }
        } else if (amount == 1) {
            if (coreTileEntity != null) {
                toDrop = coreTileEntity.unifiedExtract(itemIndex, 2);
            } else {
                toDrop = this.inventory.extractOne(itemIndex);
            }
        } else {
            if (coreTileEntity != null) {
                toDrop = coreTileEntity.unifiedExtract(itemIndex, 1);
            } else {
                toDrop = this.inventory.extractStack(itemIndex);
            }
        }
        if (toDrop != null && toDrop.stackSize > 0) {
            int maxStack = toDrop.getMaxStackSize();
            while (toDrop.stackSize > 0) {
                int dropSize = Math.min(toDrop.stackSize, maxStack);
                ItemStack dropStack = toDrop.copy();
                dropStack.stackSize = dropSize;
                toDrop.stackSize -= dropSize;
                player.dropPlayerItemWithRandomChoice(dropStack, false);
            }
            EZInventoryManager.sendToClients(inventory, coreTileEntity);
        }
    }
}
