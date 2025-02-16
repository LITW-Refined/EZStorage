package com.zerofall.ezstorage.network.server;

import com.zerofall.ezstorage.network.client.MsgReqStorage;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgReqStorage implements IMessageHandler<MsgReqStorage, MsgStorage> {

    @Override
    public MsgStorage onMessage(MsgReqStorage message, MessageContext ctx) {
        EZInventory inventory = EZInventoryManager.getInventory(message.inventoryId);
        return new MsgStorage(message.inventoryId, inventory);
    }
}
