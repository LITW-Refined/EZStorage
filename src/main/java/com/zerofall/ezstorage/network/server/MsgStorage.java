package com.zerofall.ezstorage.network.server;

import net.minecraft.nbt.NBTTagCompound;

import com.zerofall.ezstorage.util.EZInventory;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgStorage implements IMessage {

    public String storageId;
    public NBTTagCompound inventoryNbtTag;

    public MsgStorage() {}

    public MsgStorage(String storageId, NBTTagCompound inventoryNbtTag) {
        this.storageId = storageId;
        this.inventoryNbtTag = inventoryNbtTag;
    }

    public MsgStorage(EZInventory inventory) {
        this(inventory.id, inventory);
    }

    public MsgStorage(String storageId, EZInventory inventory) {
        this.storageId = storageId;
        inventoryNbtTag = new NBTTagCompound();
        if (inventory != null) {
            inventory.writeToNBT(inventoryNbtTag);
        }
    }

    public EZInventory getInventory() {
        EZInventory inventory = new EZInventory();
        if (getInventory(inventory)) {
            return inventory;
        }
        return null;
    }

    public boolean getInventory(EZInventory inventory) {
        if (inventory == null || inventoryNbtTag.hasNoTags()) {
            return false;
        }
        inventory.readFromNBT(inventoryNbtTag);
        return true;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        inventoryNbtTag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, inventoryNbtTag);
    }
}
