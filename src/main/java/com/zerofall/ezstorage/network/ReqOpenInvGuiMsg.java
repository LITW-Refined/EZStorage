package com.zerofall.ezstorage.network;

import com.zerofall.ezstorage.enums.OpenInvGuiSource;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class ReqOpenInvGuiMsg implements IMessage {

    public OpenInvGuiSource source;

    public ReqOpenInvGuiMsg() {}

    public ReqOpenInvGuiMsg(OpenInvGuiSource source) {
        this.source = source;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        source = OpenInvGuiSource.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte((byte) source.ordinal());
    }
}
