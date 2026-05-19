package com.zerofall.ezstorage.network.client;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgDropItem implements IMessage {

    public int index;
    public int amount;

    public MsgDropItem() {}

    public MsgDropItem(int index, int amount) {
        this.index = index;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = ByteBufUtils.readVarInt(buf, 5);
        amount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, index, 5);
        buf.writeInt(amount);
    }
}
