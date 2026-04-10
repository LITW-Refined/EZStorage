package com.zerofall.ezstorage.tileentity;

import net.minecraft.tileentity.TileEntity;

import com.zerofall.ezstorage.util.EZInventory;

public class TileEntityMultiblock extends TileEntity {

    private TileEntityStorageCore core;

    public void setCore(TileEntityStorageCore core) {
        this.core = core;
    }

    public TileEntityStorageCore getCore() {
        return core;
    }

    public EZInventory getInventory() {
        if (core != null) {
            return core.getInventory();
        }
        return null;
    }

    public void invalidateConnection() {
        setCore(null);;
    }
}
