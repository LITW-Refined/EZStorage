package com.zerofall.ezstorage.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.zerofall.ezstorage.block.BlockStorage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockStorage extends ItemBlock {

    public ItemBlockStorage(Block p_i45328_1_) {
        super(p_i45328_1_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> tooltip, boolean unknownFlag) {
        if (itemStack.getItem() instanceof ItemBlockStorage itemBlockStorage
            && itemBlockStorage.field_150939_a instanceof BlockStorage blockStorage) {
            tooltip.add(
                StatCollector
                    .translateToLocalFormatted("hud.msg.ezstorage.storage.capacity", blockStorage.getCapacity()));
        }
    }
}
