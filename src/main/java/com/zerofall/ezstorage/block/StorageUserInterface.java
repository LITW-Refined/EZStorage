package com.zerofall.ezstorage.block;

import com.gtnewhorizon.gtnhlib.GTNHLib;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public abstract class StorageUserInterface extends EZBlockContainer {


    protected StorageUserInterface(String name, Material materialIn) {
        super(name, materialIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
                                    float subY, float subZ) {
        if (!worldIn.isRemote && player instanceof EntityPlayerMP playerMP) {

            TileEntityStorageCore core;

            TileEntity tileEntity = worldIn.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityStorageCore coreFromTileEntity) {
                core = coreFromTileEntity;
            }
            else{
                BlockRef blockRef = new BlockRef(this, x, y, z);
                core = findCore(blockRef, worldIn, null);
            }

            if(core ==null)
                return sendNoCoreMessage(playerMP);

            EZInventory inventory = core.getInventory();
            if (inventory != null) {
                openPlayerInventoryGui(playerMP, inventory, worldIn, x, y, z, core);
            }

        }
        return true;
    }

    private boolean sendNoCoreMessage(EntityPlayerMP playerMP) {
        GTNHLib.proxy.sendMessageAboveHotbar(
            playerMP,
            new ChatComponentTranslation(
                "chat.msg.storagecore_not_found"),
            100,
            true,
            true);
        return true;
    }
}
