package com.zerofall.ezstorage.network.client;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgPullBookmarkItems implements IMessage {

    public NBTTagCompound items;

    public MsgPullBookmarkItems() {}

    public MsgPullBookmarkItems(NBTTagCompound items) {
        this.items = items;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        items = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.items);
    }
}
