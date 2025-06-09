package com.zerofall.ezstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.server.MsgStorage;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventory;

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

    public void openPlayerInventoryGui(EntityPlayerMP playerMP, EZInventory inventory, World worldIn, int x, int y,
        int z, TileEntityStorageCore core) {
        boolean enableCraftingGrid = core.hasCraftBox && !IntegrationUtils.isSpectatorMode(playerMP);
        EZStorage.instance.guiHandler.inventoryIds.put(playerMP, inventory.id);
        playerMP.openGui(EZStorage.instance, enableCraftingGrid ? 2 : 1, worldIn, x, y, z);
        EZStorage.instance.network.sendTo(new MsgStorage(inventory), playerMP);
    }

}
