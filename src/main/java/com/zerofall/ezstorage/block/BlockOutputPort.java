package com.zerofall.ezstorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.tileentity.TileEntityOutputPort;

public class BlockOutputPort extends EZBlockContainer {

    public BlockOutputPort() {
        super("output_port", Material.iron);
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        return new TileEntityOutputPort();
    }
}
