package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayer;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.network.client.MsgClearCraftingGrid;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgClearCraftingGrid implements IMessageHandler<MsgClearCraftingGrid, IMessage> {

    @Override
    public IMessage onMessage(MsgClearCraftingGrid message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null && player.openContainer instanceof ContainerStorageCoreCrafting container) {
            container.clearGrid(player);
        }
        return null;
    }
}
