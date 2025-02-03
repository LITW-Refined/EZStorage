package com.zerofall.ezstorage.item;

import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemPortableStoragePanel extends EZItem {

    public ItemPortableStoragePanel() {
        super("portable_storage_terminal");
        setMaxStackSize(1);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int meta, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (itemStackIn.getItem() instanceof ItemPortableStoragePanel panel) {
            TileEntity block = worldIn.getTileEntity(x, y, z);
            if (block instanceof TileEntityStorageCore core) {
                // Open Gui
            }
        }
        return null;
    }
}
