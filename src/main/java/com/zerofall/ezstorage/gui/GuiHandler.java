package com.zerofall.ezstorage.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            if (ID == 1) {
                return new ContainerStorageCore(player, world, x, y, z);
            } else if (ID == 2) {
                return new ContainerStorageCoreCrafting(player, world, x, y, z);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            if (ID == 1) {
                return new GuiStorageCore(player, world, x, y, z);
            } else if (ID == 2) {
                return new GuiCraftingCore(player, world, x, y, z);
            }
        }
        return null;
    }

}
