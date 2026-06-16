package com.zerofall.ezstorage.network.client;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgAutoCraft implements IMessage {

    public NBTTagCompound recipe;
    public int count;

    public MsgAutoCraft() {}

    public MsgAutoCraft(NBTTagCompound recipe, int count) {
        this.recipe = recipe;
        this.count = count;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        recipe = ByteBufUtils.readTag(buf);
        count = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, recipe);
        buf.writeInt(count);
    }
}
