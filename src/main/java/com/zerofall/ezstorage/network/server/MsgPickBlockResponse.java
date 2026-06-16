package com.zerofall.ezstorage.network.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Server→Client response for pick-block operation. Carries the target hotbar slot index (0-8)
 * so the client can switch its selected slot.
 */
public class MsgPickBlockResponse implements IMessage {

    public int targetSlot;

    public MsgPickBlockResponse() {}

    public MsgPickBlockResponse(int targetSlot) {
        this.targetSlot = targetSlot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        targetSlot = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte((byte) targetSlot);
    }
}
