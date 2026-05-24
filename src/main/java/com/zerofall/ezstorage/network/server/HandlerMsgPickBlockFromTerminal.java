package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import com.gtnewhorizon.gtnhlib.GTNHLib;
import com.zerofall.ezstorage.integration.ModIds;
import com.zerofall.ezstorage.item.ItemPortableStoragePanel;
import com.zerofall.ezstorage.network.client.MsgPickBlockFromTerminal;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.EZInventoryReference;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgPickBlockFromTerminal implements IMessageHandler<MsgPickBlockFromTerminal, IMessage> {

    @Override
    public IMessage onMessage(MsgPickBlockFromTerminal message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        ItemStack targetStack = message.targetStack;

        if (targetStack == null) {
            return null;
        }

        // Check Baubles is loaded
        if (!ModIds.BAUBLES.isLoaded()) {
            return null;
        }

        // Get portable storage panel from Baubles
        ItemStack panelStack = ItemPortableStoragePanel.getFromBaubles(player);
        if (panelStack == null || !(panelStack.getItem() instanceof ItemPortableStoragePanel)) {
            return null;
        }

        ItemPortableStoragePanel panel = (ItemPortableStoragePanel) panelStack.getItem();

        // Validate reference
        if (!panel.validateReference(panelStack)) {
            GTNHLib.proxy.sendMessageAboveHotbar(
                player,
                new ChatComponentTranslation("chat.msg.storagecore_not_found"),
                60,
                true,
                true);
            return null;
        }

        EZInventoryReference reference = panel.getInventoryReference(panelStack);
        if (reference == null) {
            return null;
        }

        // Check range
        if (!ItemPortableStoragePanel.isInRange(panelStack, reference, player)) {
            GTNHLib.proxy.sendMessageAboveHotbar(
                player,
                new ChatComponentTranslation("chat.msg.storagecore_out_of_range"),
                60,
                true,
                true);
            return null;
        }

        // Get inventory
        EZInventory inventory = EZInventoryManager.getInventory(reference.inventoryId);
        if (inventory == null) {
            return null;
        }

        // Find matching item in inventory
        int index = inventory.getIndexOf(targetStack);
        if (index < 0) {
            return null;
        }

        // Extract up to one stack
        int maxSize = targetStack.getMaxStackSize();
        ItemStack extracted = inventory.getItemStackAt(index, maxSize);
        if (extracted == null || extracted.stackSize <= 0) {
            return null;
        }

        // ── Place extracted item into player hotbar ──
        int targetSlot = player.inventory.currentItem;
        boolean placed = false;

        // Pass 1: merge into existing same-type hotbar stacks (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack hotbarStack = player.inventory.getStackInSlot(i);
            if (hotbarStack != null && EZInventory.stacksEqual(hotbarStack, extracted)) {
                int space = hotbarStack.getMaxStackSize() - hotbarStack.stackSize;
                if (space > 0) {
                    int toMerge = Math.min(space, extracted.stackSize);
                    hotbarStack.stackSize += toMerge;
                    extracted.stackSize -= toMerge;
                    targetSlot = i;
                    placed = true;
                    if (extracted.stackSize <= 0) {
                        break;
                    }
                }
            }
        }

        // Pass 2: place in empty hotbar slot
        if (extracted.stackSize > 0) {
            for (int i = 0; i < 9; i++) {
                if (player.inventory.getStackInSlot(i) == null) {
                    player.inventory.setInventorySlotContents(i, extracted.copy());
                    targetSlot = i;
                    extracted.stackSize = 0;
                    placed = true;
                    break;
                }
            }
        }

        // Pass 3: hotbar full — use the current selected slot
        if (!placed && extracted.stackSize > 0) {
            int curSlot = player.inventory.currentItem;
            ItemStack heldStack = player.inventory.getStackInSlot(curSlot);

            if (heldStack != null) {
                // Move held item to backpack (slots 9-35)
                boolean backpackPlaced = false;

                // 3a: merge into existing same-type backpack stacks
                for (int i = 9; i < 36; i++) {
                    ItemStack backpackStack = player.inventory.getStackInSlot(i);
                    if (backpackStack != null && EZInventory.stacksEqual(backpackStack, heldStack)) {
                        int space = backpackStack.getMaxStackSize() - backpackStack.stackSize;
                        if (space > 0) {
                            int toMerge = Math.min(space, heldStack.stackSize);
                            backpackStack.stackSize += toMerge;
                            heldStack.stackSize -= toMerge;
                            if (heldStack.stackSize <= 0) {
                                backpackPlaced = true;
                                break;
                            }
                        }
                    }
                }

                // 3b: place in empty backpack slot
                if (!backpackPlaced && heldStack.stackSize > 0) {
                    for (int i = 9; i < 36; i++) {
                        if (player.inventory.getStackInSlot(i) == null) {
                            player.inventory.setInventorySlotContents(i, heldStack.copy());
                            heldStack.stackSize = 0;
                            backpackPlaced = true;
                            break;
                        }
                    }
                }

                // 3c: backpack also full — abort
                if (!backpackPlaced) {
                    // Return extracted items to inventory
                    inventory.input(extracted);
                    inventory.setHasChanges();
                    EZInventoryManager.sendToClients(inventory);

                    GTNHLib.proxy.sendMessageAboveHotbar(
                        player,
                        new ChatComponentTranslation("chat.msg.ezstorage.inventory_full"),
                        60,
                        true,
                        true);
                    return new MsgPickBlockResponse(player.inventory.currentItem);
                }

                // Clear the current slot now that held item is moved
                player.inventory.setInventorySlotContents(curSlot, null);
            }

            // Place extracted in the current slot
            player.inventory.setInventorySlotContents(curSlot, extracted.copy());
            extracted.stackSize = 0;
            targetSlot = curSlot;
            placed = true;
        }

        // Return any excess to inventory (shouldn't normally remain)
        if (extracted != null && extracted.stackSize > 0) {
            inventory.input(extracted);
        }

        // Switch server-side held slot
        player.inventory.currentItem = targetSlot;

        // Save & sync
        inventory.setHasChanges();
        EZInventoryManager.sendToClients(inventory);

        // Tell client which slot to switch to
        return new MsgPickBlockResponse(targetSlot);
    }
}
