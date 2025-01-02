package com.zerofall.ezstorage.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class RecipePacketHandler implements IMessageHandler<RecipeMessage, IMessage> {

    ItemStack[][] recipe;

    @Override
    public IMessage onMessage(RecipeMessage message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container container = player.openContainer;
        if (container instanceof ContainerStorageCoreCrafting) {
            ContainerStorageCoreCrafting con = (ContainerStorageCoreCrafting) container;
            TileEntityStorageCore tileEntity = con.tileEntity;

            // Empty grid into inventory
            for (int i = 0; i < 9; i++) {
                ItemStack stack = con.craftMatrix.getStackInSlot(i);
                if (stack != null) {
                    ItemStack results = tileEntity.input(stack);
                    if (results != null) {
                        return null;
                    }
                    con.craftMatrix.setInventorySlotContents(i, null);
                }
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
            for (int i = 0; i < this.recipe.length; i++) {
                if (this.recipe[i] != null && this.recipe[i].length > 0) {
                    Slot slot = con.getSlotFromInventory(con.craftMatrix, i);
                    if (slot != null) {
                        ItemStack retreived = tileEntity.inventory.getItems(this.recipe[i]);
                        if (retreived != null) {
                            slot.putStack(retreived);
                        }
                    }
                }
            }
            tileEntity.updateTileEntity();
        }

        return null;
    }

}
