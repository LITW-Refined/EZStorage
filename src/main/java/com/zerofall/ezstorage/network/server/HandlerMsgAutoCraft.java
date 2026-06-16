package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.nei.NeiCraftingOverlay;
import com.zerofall.ezstorage.network.client.MsgAutoCraft;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgAutoCraft implements IMessageHandler<MsgAutoCraft, IMessage> {

    private ItemStack[][] recipe;

    @Override
    public IMessage onMessage(MsgAutoCraft message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)) {
            return null;
        }
        Container container = player.openContainer;
        if (!(container instanceof ContainerStorageCoreCrafting con)) {
            return null;
        }

        this.recipe = new ItemStack[9][];
        for (int x = 0; x < this.recipe.length; x++) {
            NBTTagList list = message.recipe.getTagList("#" + x, 10);
            if (list.tagCount() > 0) {
                this.recipe[x] = new ItemStack[list.tagCount()];
                for (int y = 0; y < list.tagCount(); y++) {
                    this.recipe[x][y] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(y));
                }
            }
        }

        int craftsRemaining = message.count;
        boolean hasChanges = false;

        while (craftsRemaining > 0) {
            ItemStack[] extractedMaterials = new ItemStack[9];
            boolean[] fromPlayerInv = new boolean[9];
            boolean allFound = true;

            for (int slot = 0; slot < this.recipe.length; slot++) {
                ItemStack[] recipeItems = this.recipe[slot];
                if (recipeItems == null || recipeItems.length == 0) continue;

                ItemStack matched = null;
                for (ItemStack ri : recipeItems) {
                    if (ri == null) continue;
                    int needed = ri.stackSize > 0 ? ri.stackSize : 1;
                    ItemStack one = ri.copy();
                    one.stackSize = needed;

                    matched = extractFromStorage(con.inventory, one, needed);
                    if (matched == null) {
                        matched = extractFromPlayerInventory(player, one, needed);
                        if (matched != null) {
                            fromPlayerInv[slot] = true;
                        }
                    }
                    if (matched != null) break;
                }

                if (matched != null) {
                    extractedMaterials[slot] = matched;
                } else {
                    allFound = false;
                    break;
                }
            }

            if (!allFound) {
                returnExtractedToSource(con.inventory, player, extractedMaterials, fromPlayerInv);
                break;
            }

            InventoryCrafting tempCraft = new InventoryCrafting(new Container() {

                @Override
                public boolean canInteractWith(EntityPlayer playerIn) {
                    return false;
                }
            }, 3, 3);
            for (int i = 0; i < 9; i++) {
                if (extractedMaterials[i] != null) {
                    tempCraft.setInventorySlotContents(i, extractedMaterials[i].copy());
                }
            }

            ItemStack result = CraftingManager.getInstance()
                .findMatchingRecipe(tempCraft, player.worldObj);

            if (result == null) {
                returnExtractedToSource(con.inventory, player, extractedMaterials, fromPlayerInv);
                break;
            }

            handleContainerItems(con.inventory, extractedMaterials);

            result = result.copy();
            if (!player.inventory.addItemStackToInventory(result)) {
                ItemStack leftover = con.inventory.input(result);
                if (leftover != null) {
                    player.dropPlayerItemWithRandomChoice(leftover, false);
                }
            }

            craftsRemaining--;
            hasChanges = true;
        }

        if (hasChanges) {
            EZInventoryManager.sendToClients(con.inventory);
        }
        return null;
    }

    private static void handleContainerItems(EZInventory inventory, ItemStack[] materials) {
        for (int i = 0; i < materials.length; i++) {
            ItemStack material = materials[i];
            if (material == null) continue;

            if (NeiCraftingOverlay.isGTTool(material)) {
                NeiCraftingOverlay.incrementGTDamage(material, 1);
                if (NeiCraftingOverlay.getGTDurability(material) > 0) {
                    inventory.input(material);
                }
                continue;
            }

            if (material.getItem()
                .hasContainerItem(material)) {
                ItemStack container = material.getItem()
                    .getContainerItem(material);
                if (container != null) {
                    if (container.getItem() == material.getItem()) {
                        if (!container.isItemStackDamageable()
                            || container.getItemDamage() <= container.getMaxDamage()) {
                            inventory.input(container.copy());
                        }
                    } else {
                        inventory.input(container.copy());
                    }
                }
            }
        }
    }

    private static ItemStack extractFromStorage(EZInventory inventory, ItemStack recipeItem, int amount) {
        for (int i = 0; i < inventory.inventory.size(); i++) {
            ItemStack group = inventory.inventory.get(i);
            if (ContainerStorageCoreCrafting.isRecipeItemValid(recipeItem, group) && group.stackSize >= amount) {
                ItemStack extracted = group.copy();
                extracted.stackSize = amount;
                group.stackSize -= amount;
                if (group.stackSize <= 0) {
                    inventory.inventory.remove(i);
                }
                inventory.setHasChanges();
                return extracted;
            }
        }
        return null;
    }

    private static ItemStack extractFromPlayerInventory(EntityPlayerMP player, ItemStack recipeItem, int amount) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null && ContainerStorageCoreCrafting.isRecipeItemValid(recipeItem, stack)
                && stack.stackSize >= amount) {
                return player.inventory.decrStackSize(i, amount);
            }
        }
        return null;
    }

    private static void returnExtractedToSource(EZInventory inventory, EntityPlayer player, ItemStack[] extracted,
        boolean[] fromPlayerInv) {
        for (int i = 0; i < extracted.length; i++) {
            ItemStack stack = extracted[i];
            if (stack != null) {
                if (fromPlayerInv[i]) {
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        inventory.input(stack);
                    }
                } else {
                    inventory.input(stack);
                }
            }
        }
    }
}
