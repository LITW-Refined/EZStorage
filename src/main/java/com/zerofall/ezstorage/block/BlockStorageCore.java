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
        if (te instanceof TileEntityStorageCore core) {
            EZInventory inventory = EZInventoryManager.getInventory(core.inventoryId);
            if (inventory != null) {
                EZInventoryManager.deleteInventory(inventory);
            }
        }
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (!worldIn.isRemote && player instanceof EntityPlayerMP playerMP) {
            TileEntity tileEntity = worldIn.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityStorageCore core) {
                EZInventory inventory = core.getInventory();
                EZStorage.instance.guiHandler.inventoryIds.put(playerMP, inventory.id);
                player.openGui(
                    EZStorage.instance,
                    core.hasCraftBox && !IntegrationUtils.isSpectatorMode(playerMP) ? 2 : 1,
                    worldIn,
                    x,
                    y,
                    z);
                EZStorage.instance.network.sendTo(new MsgStorage(inventory), playerMP);
            }
        }
        return true;
    }

}
