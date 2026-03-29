package com.zerofall.ezstorage.integration.ae2;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.storage.MEMonitorIInventory;

public class AE2StorageHandler implements IExternalStorageHandler {

    @Override
    public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc) {
        return te instanceof TileEntityInventoryProxy && channel == StorageChannel.ITEMS;
    }

    @Override
    public IMEInventory<IAEItemStack> getInventory(TileEntity te, ForgeDirection d, StorageChannel channel,
        BaseActionSource src) {
        if (channel != StorageChannel.ITEMS) {
            return null;
        }
        if (te instanceof TileEntityInventoryProxy teInvProxy) {
            return new MEMonitorIInventory(new EZStorageMEAdapter(teInvProxy));
        }
        return null;
    }

}
