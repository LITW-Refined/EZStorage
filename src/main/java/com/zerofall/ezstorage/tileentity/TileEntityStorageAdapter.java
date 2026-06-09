package com.zerofall.ezstorage.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import com.zerofall.ezstorage.block.StorageMultiblock;
import com.zerofall.ezstorage.util.BlockRef;

public class TileEntityStorageAdapter extends TileEntityMultiblock {

    private List<BlockRef> connectedInventories = new ArrayList<>();

    public void scanConnectedInventories() {
        List<BlockRef> newConnected = new ArrayList<>();
        int[][] directions = { { -1, 0, 0 }, { 1, 0, 0 }, { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 } };

        for (int[] dir : directions) {
            int nx = xCoord + dir[0];
            int ny = yCoord + dir[1];
            int nz = zCoord + dir[2];

            TileEntity te = worldObj.getTileEntity(nx, ny, nz);
            if (te instanceof IInventory) {
                // Skip multiblock components — we don't want to connect to our own system
                if (worldObj.getBlock(nx, ny, nz) instanceof StorageMultiblock) {
                    continue;
                }
                newConnected.add(new BlockRef(worldObj.getBlock(nx, ny, nz), nx, ny, nz));
            }
        }

        connectedInventories = newConnected;
    }

    public List<BlockRef> getConnectedInventories() {
        return connectedInventories;
    }

    @Override
    public void updateEntity() {
        if (!worldObj.isRemote && worldObj.getTotalWorldTime() % 40 == 0) {
            scanConnectedInventories();
        }
    }

    @Override
    public void setCore(TileEntityStorageCore core) {
        super.setCore(core);
        if (core != null && !worldObj.isRemote) {
            scanConnectedInventories();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (BlockRef ref : connectedInventories) {
            NBTTagCompound refTag = new NBTTagCompound();
            refTag.setInteger("x", ref.posX);
            refTag.setInteger("y", ref.posY);
            refTag.setInteger("z", ref.posZ);
            list.appendTag(refTag);
        }
        tag.setTag("connectedInventories", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        connectedInventories.clear();
        NBTTagList list = tag.getTagList("connectedInventories", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound refTag = list.getCompoundTagAt(i);
            int x = refTag.getInteger("x");
            int y = refTag.getInteger("y");
            int z = refTag.getInteger("z");
            connectedInventories.add(new BlockRef(worldObj.getBlock(x, y, z), x, y, z));
        }
    }
}
