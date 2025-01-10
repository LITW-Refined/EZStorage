package com.zerofall.ezstorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;

public class BlockInputPort extends EZBlockContainer {

    public BlockInputPort() {
        super("input_port", Material.iron);
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        return new TileEntityInventoryProxy();
    }
}
