package com.zerofall.ezstorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.tileentity.TileEntityStorageAdapter;

public class BlockStorageAdapter extends EZBlockContainer {

    public BlockStorageAdapter() {
        super("storage_adapter", Material.iron);
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        return new TileEntityStorageAdapter();
    }
}
