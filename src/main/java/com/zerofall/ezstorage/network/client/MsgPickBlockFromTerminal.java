package com.zerofall.ezstorage.network.client;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgPickBlockFromTerminal implements IMessage {

    public ItemStack targetStack;

    public MsgPickBlockFromTerminal() {}

    public MsgPickBlockFromTerminal(ItemStack targetStack) {
        this.targetStack = targetStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        targetStack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, targetStack);
    }
}
