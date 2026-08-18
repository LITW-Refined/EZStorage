package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgPullBookmarkItems;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgPullBookmarkItems implements IMessageHandler<MsgPullBookmarkItems, IMessage> {

    @Override
    public IMessage onMessage(MsgPullBookmarkItems message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (!IntegrationUtils.isSpectatorMode(player)
            && player.openContainer instanceof ContainerStorageCore storageContainer
            && message.items != null) {
            NBTTagList list = message.items.getTagList("items", 10);
            storageContainer.pullBookmarkItems(list, player);
        }
        return null;
    }
}
