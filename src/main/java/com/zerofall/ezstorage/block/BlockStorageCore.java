package com.zerofall.ezstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

public class BlockStorageCore extends EZBlockContainer {

    public BlockStorageCore() {
        super("storage_core", Material.wood);
        this.setResistance(6000.0f);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityStorageCore();
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te instanceof TileEntityStorageCore && ((TileEntityStorageCore) te).inventory.getTotalCount() > 0) {
            super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (!worldIn.isRemote) {
            TileEntityStorageCore tileEntity = (TileEntityStorageCore) worldIn.getTileEntity(x, y, z);
            if (tileEntity.hasCraftBox) {
                player.openGui(EZStorage.instance, 2, worldIn, x, y, z);
            } else {
                player.openGui(EZStorage.instance, 1, worldIn, x, y, z);
            }

        }
        return true;
    }

}
