package com.zerofall.ezstorage.network.server;

import net.minecraft.nbt.NBTTagCompound;

import com.zerofall.ezstorage.util.EZInventory;

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
        int length = buf.readInt();
        if (length > 0) {
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            try {
                inventoryNbtTag = net.minecraft.nbt.CompressedStreamTools
                    .func_152457_a(bytes, net.minecraft.nbt.NBTSizeTracker.field_152451_a);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (inventoryNbtTag != null) {
                byte[] bytes = net.minecraft.nbt.CompressedStreamTools.compress(inventoryNbtTag);
                buf.writeInt(bytes.length);
                buf.writeBytes(bytes);
            } else {
                buf.writeInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
