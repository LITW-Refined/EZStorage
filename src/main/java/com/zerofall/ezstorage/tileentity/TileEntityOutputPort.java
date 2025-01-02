package com.zerofall.ezstorage.tileentity;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;

import com.zerofall.ezstorage.util.ItemGroup;

public class TileEntityOutputPort extends TileEntity implements IUpdatePlayerListBox {

    public TileEntityStorageCore core;

    @Override
    public void update() {
        if (core != null && !worldObj.isRemote) {
            boolean updateCore = false;
            TileEntity tileentity = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
            if (tileentity instanceof IInventory) {
                IInventory inventory = (IInventory) tileentity;

                if (inventory != null) {
                    List<ItemGroup> inventoryList = core.inventory.inventory;
                    if (inventoryList != null && inventoryList.size() > 0) {
                        ItemGroup group = inventoryList.get(0);
                        if (group != null) {
                            ItemStack stack = group.itemStack;
                            stack.stackSize = (int) Math.min((long) stack.getMaxStackSize(), group.count);
                            int stackSize = stack.stackSize;
                            ItemStack leftOver = TileEntityHopper.func_145889_a(inventory, stack, 0);
                            if (leftOver != null) {
                                int remaining = stackSize - leftOver.stackSize;
                                if (remaining > 0) {
                                    group.count -= remaining;
                                    updateCore = true;
                                }
                            } else {
                                group.count -= stackSize;
                                updateCore = true;
                            }
                            if (group.count <= 0) {
                                core.inventory.inventory.remove(0);
                            }
                        }
                    }
                }
            }
            if (updateCore) {
                this.worldObj.markBlockForUpdate(core.xCoord, core.yCoord, core.zCoord);
                core.markDirty();
            }
        }
    }
}
