package com.zerofall.ezstorage.network.client;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgReqStorage implements IMessage {

    public String inventoryId;

    public MsgReqStorage() {}

    public MsgReqStorage(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, inventoryId);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        inventoryId = ByteBufUtils.readUTF8String(buf);
    }
}
