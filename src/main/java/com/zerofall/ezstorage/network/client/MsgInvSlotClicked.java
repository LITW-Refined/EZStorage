package com.zerofall.ezstorage.network.client;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgInvSlotClicked implements IMessage {

    public int index;
    public int button;
    public int mode;

    public MsgInvSlotClicked() {}

    public MsgInvSlotClicked(int index, int button, int mode) {
        this.index = index;
        this.button = button;
        this.mode = mode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = ByteBufUtils.readVarInt(buf, 5);
        button = ByteBufUtils.readVarInt(buf, 5);
        mode = ByteBufUtils.readVarInt(buf, 5);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, index, 5);
        ByteBufUtils.writeVarInt(buf, button, 5);
        ByteBufUtils.writeVarInt(buf, mode, 5);
    }
}
