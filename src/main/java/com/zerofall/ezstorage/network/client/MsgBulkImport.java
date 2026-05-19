package com.zerofall.ezstorage.network.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgBulkImport implements IMessage {

    public boolean hotbarOnly;

    public MsgBulkImport() {}

    public MsgBulkImport(boolean hotbarOnly) {
        this.hotbarOnly = hotbarOnly;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hotbarOnly = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(hotbarOnly);
    }
}
