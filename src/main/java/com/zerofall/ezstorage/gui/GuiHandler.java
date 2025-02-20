package com.zerofall.ezstorage.gui;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public HashMap<EntityPlayer, String> inventoryIds = new HashMap<EntityPlayer, String>();

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (inventoryIds.containsKey(player)) {
            String inventoryId = inventoryIds.remove(player);
            EZInventory inventory = EZInventoryManager.getInventory(inventoryId);
            if (inventory != null) {
                if (ID == 1) {
                    return new ContainerStorageCore(player, inventory);
                } else if (ID == 2) {
                    return new ContainerStorageCoreCrafting(player, world, inventory);
                }
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 1) {
            return new GuiStorageCore(player, world, x, y, z);
        } else if (ID == 2) {
            return new GuiCraftingCore(player, world, x, y, z);
        }
        return null;
    }
}
