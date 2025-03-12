package com.zerofall.ezstorage.network.client;

import java.time.LocalDateTime;

import net.minecraft.client.entity.EntityClientPlayerMP;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.network.server.MsgStorage;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgStorage implements IMessageHandler<MsgStorage, IMessage> {

    @Override
    public IMessage onMessage(MsgStorage message, MessageContext ctx) {
        EntityClientPlayerMP player = FMLClientHandler.instance()
            .getClientPlayerEntity();
        if (player.openContainer instanceof ContainerStorageCore container) {
            message.getInventory(container.inventory);
            container.inventoryUpdateTimestamp = LocalDateTime.now();
        }
        return null;
    }
}
