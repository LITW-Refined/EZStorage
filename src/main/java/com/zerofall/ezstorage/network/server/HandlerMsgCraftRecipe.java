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
import com.zerofall.ezstorage.network.client.MsgCraftRecipe;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgCraftRecipe implements IMessageHandler<MsgCraftRecipe, IMessage> {

    @Override
    public IMessage onMessage(MsgCraftRecipe message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)
            || !(player.openContainer instanceof ContainerStorageCoreCrafting container)) {
            return null;
        }

        ItemStack[][] recipe = parseRecipe(message.recipe);
        boolean hasChanges = false;

        for (int crafted = 0; crafted < message.count; crafted++) {
            if (!craftOne(recipe, container.inventory, player)) {
                break;
            }
            hasChanges = true;
        }

        if (hasChanges) {
            EZInventoryManager.sendToClients(container.inventory);
        }
        return null;
    }

    private static ItemStack[][] parseRecipe(net.minecraft.nbt.NBTTagCompound recipeNBT) {
        ItemStack[][] recipe = new ItemStack[9][];
        for (int slot = 0; slot < recipe.length; slot++) {
            NBTTagList alternatives = recipeNBT.getTagList("#" + slot, 10);
            if (alternatives.tagCount() == 0) {
                continue;
            }
            recipe[slot] = new ItemStack[alternatives.tagCount()];
            for (int i = 0; i < alternatives.tagCount(); i++) {
                recipe[slot][i] = ItemStack.loadItemStackFromNBT(alternatives.getCompoundTagAt(i));
            }
        }
        return recipe;
    }

    private static boolean craftOne(ItemStack[][] recipe, EZInventory inventory, EntityPlayerMP player) {
        ItemStack[] extracted = new ItemStack[9];
        boolean[] fromPlayerInv = new boolean[9];

        for (int slot = 0; slot < recipe.length; slot++) {
            ItemStack[] alternatives = recipe[slot];
            if (alternatives == null || alternatives.length == 0) {
                continue;
            }

            ItemStack matched = null;
            for (ItemStack alternative : alternatives) {
                if (alternative == null) {
                    continue;
                }
                int needed = alternative.stackSize > 0 ? alternative.stackSize : 1;
                matched = ContainerStorageCoreCrafting.getMatchingItemFromStorage(inventory, alternative, needed);
                if (matched == null) {
                    matched = ContainerStorageCoreCrafting
                        .takeFromPlayerInventory(player.inventory, alternative, needed);
                    if (matched != null) {
                        fromPlayerInv[slot] = true;
                    }
                }
                if (matched != null) {
                    break;
                }
            }

            if (matched == null) {
                returnExtracted(inventory, player, extracted, fromPlayerInv);
                return false;
            }
            extracted[slot] = matched;
        }

        InventoryCrafting tempGrid = new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, 3, 3);
        for (int slot = 0; slot < extracted.length; slot++) {
            if (extracted[slot] != null) {
                tempGrid.setInventorySlotContents(slot, extracted[slot].copy());
            }
        }

        ItemStack result = CraftingManager.getInstance()
            .findMatchingRecipe(tempGrid, player.worldObj);
        if (result == null) {
            returnExtracted(inventory, player, extracted, fromPlayerInv);
            return false;
        }

        returnContainerItems(inventory, extracted);

        result = result.copy();
        if (!player.inventory.addItemStackToInventory(result)) {
            ItemStack leftover = inventory.input(result);
            if (leftover != null) {
                player.dropPlayerItemWithRandomChoice(leftover, false);
            }
        }
        return true;
    }

    private static void returnContainerItems(EZInventory inventory, ItemStack[] extracted) {
        for (ItemStack material : extracted) {
            ItemStack toReturn = ContainerStorageCoreCrafting.afterCraftingUse(material);
            if (toReturn != null) {
                inventory.input(toReturn);
            }
        }
    }

    private static void returnExtracted(EZInventory inventory, EntityPlayer player, ItemStack[] extracted,
        boolean[] fromPlayerInv) {
        for (int slot = 0; slot < extracted.length; slot++) {
            ItemStack stack = extracted[slot];
            if (stack == null) {
                continue;
            }
            if (fromPlayerInv[slot] && player.inventory.addItemStackToInventory(stack)) {
                continue;
            }
            inventory.input(stack);
        }
    }
}
