package com.zerofall.ezstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.EZStorage;

public class EZBlockContainer extends StorageMultiblock implements ITileEntityProvider {

    protected EZBlockContainer(String name, Material materialIn) {
        super(name, materialIn);
        this.setBlockName(name);
        this.setCreativeTab(EZStorage.instance.creativeTab);
        this.isBlockContainer = true;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        worldIn.removeTileEntity(x, y, z);
    }

    /**
     * Called on both Client and Server when World#addBlockEvent is called
     */
    @Override
    public boolean onBlockEventReceived(World worldIn, int x, int y, int z, int eventId, int eventData) {
        super.onBlockEventReceived(worldIn, x, y, z, eventId, eventData);
        TileEntity tileentity = worldIn.getTileEntity(x, y, z);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventId, eventData);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

}
