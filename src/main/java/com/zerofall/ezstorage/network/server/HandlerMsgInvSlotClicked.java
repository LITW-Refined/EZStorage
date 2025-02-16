package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayer;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgInvSlotClicked;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgInvSlotClicked implements IMessageHandler<MsgInvSlotClicked, IMessage> {

    @Override
    public IMessage onMessage(MsgInvSlotClicked message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (!IntegrationUtils.isSpectatorMode(player)
            && player.openContainer instanceof ContainerStorageCore storageContainer) {
            storageContainer.customSlotClick(message.index, message.button, message.mode, player);
            return new MsgStorage(storageContainer.inventory.id, storageContainer.inventory);
        }
        return null; // no response in this case
    }

}
