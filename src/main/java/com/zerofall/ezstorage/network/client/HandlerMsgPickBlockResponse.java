package com.zerofall.ezstorage.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.zerofall.ezstorage.network.server.MsgPickBlockResponse;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side handler for pick-block response. Switches the player's selected hotbar slot.
 */
public class HandlerMsgPickBlockResponse implements IMessageHandler<MsgPickBlockResponse, IMessage> {

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MsgPickBlockResponse message, MessageContext ctx) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            int slot = message.targetSlot;
            if (slot >= 0 && slot <= 8) {
                player.inventory.currentItem = slot;
            }
        }
        return null;
    }
}
