package com.zerofall.ezstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.server.MsgStorage;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

public class BlockStorageCore extends StorageUserInterface {

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
        if (te instanceof TileEntityStorageCore core) {
            EZInventory inventory = EZInventoryManager.getInventory(core.inventoryId);
            if (inventory != null) {
                EZInventoryManager.deleteInventory(inventory);
            }
        }
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }
}
