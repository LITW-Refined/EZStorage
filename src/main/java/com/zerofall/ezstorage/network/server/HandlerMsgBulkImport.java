package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayerMP;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgBulkImport;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgBulkImport implements IMessageHandler<MsgBulkImport, IMessage> {

    @Override
    public IMessage onMessage(MsgBulkImport message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (!IntegrationUtils.isSpectatorMode(player)
            && player.openContainer instanceof ContainerStorageCore container) {
            container.importPlayerInventory(player, message.hotbarOnly);
        }
        return null;
    }
}
