package com.zerofall.ezstorage.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.zerofall.ezstorage.item.ItemPortableStoragePanel;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ReqOpenInvGuiMsgHandler implements IMessageHandler<ReqOpenInvGuiMsg, IMessage> {

    @Override
    public IMessage onMessage(ReqOpenInvGuiMsg message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;

        switch (message.source) {
            case BAUBLES:
                ItemStack itemStack = ItemPortableStoragePanel.getFromBaubles(player);
                if (itemStack != null && itemStack.getItem() instanceof ItemPortableStoragePanel panel) {
                    panel.onItemRightClick(itemStack, player.getEntityWorld(), player);
                }
                break;
        }

        return null;
    }

}
