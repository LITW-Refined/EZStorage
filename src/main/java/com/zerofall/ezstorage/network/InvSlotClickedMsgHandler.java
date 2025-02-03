package com.zerofall.ezstorage.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.integration.IntegrationUtils;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class InvSlotClickedMsgHandler implements IMessageHandler<InvSlotClickedMsg, IMessage> {

    @Override
    public IMessage onMessage(InvSlotClickedMsg message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)) {
            return null; // no response, we're in read-only mode
        }
        Container container = player.openContainer;
        if (container != null && container instanceof ContainerStorageCore storageContainer) {
            storageContainer.customSlotClick(message.index, message.button, message.mode, player);
        }
        return null; // no response in this case
    }

}
