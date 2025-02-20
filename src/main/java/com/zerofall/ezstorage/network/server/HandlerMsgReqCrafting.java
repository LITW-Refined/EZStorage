package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgReqCrafting;
import com.zerofall.ezstorage.util.EZInventory;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgReqCrafting implements IMessageHandler<MsgReqCrafting, IMessage> {

    ItemStack[][] recipe;

    @Override
    public IMessage onMessage(MsgReqCrafting message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)) {
            return null; // no response, we're in read-only mode
        }
        Container container = player.openContainer;
        if (container instanceof ContainerStorageCoreCrafting con) {
            EZInventory inventory = con.inventory;

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
            for (int i = 0; i < this.recipe.length; i++) {
                Slot slot = con.getSlotFromInventory(con.craftMatrix, i);
                if (slot != null) {
                    ItemStack slotStack = slot.getStack();

                    if (i < this.recipe.length && this.recipe[i] != null && this.recipe[i].length > 0) {
                        boolean isValid = false;

                        if (slotStack != null) {
                            for (ItemStack recipeStack : this.recipe[i]) {
                                if (recipeStack.isItemEqual(slot.getStack())) {
                                    isValid = true;
                                }
                            }
                        }

                        if (isValid) {
                            continue;
                        }

                        ItemStack retreived = inventory.getItems(this.recipe[i]);
                        if (retreived != null) {
                            slot.putStack(retreived);
                            continue;
                        }
                    }

                    if (slotStack != null) {
                        slot.putStack(null);
                    }
                }
            }
            return new MsgStorage(inventory.id, inventory);
        }

        return null;
    }

}
