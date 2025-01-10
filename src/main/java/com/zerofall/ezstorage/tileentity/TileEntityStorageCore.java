package com.zerofall.ezstorage.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

import com.zerofall.ezstorage.block.BlockCraftingBox;
import com.zerofall.ezstorage.block.BlockInputPort;
import com.zerofall.ezstorage.block.BlockStorage;
import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.block.StorageMultiblock;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZStorageUtils;
import com.zerofall.ezstorage.util.ItemGroup;

public class TileEntityStorageCore extends TileEntity {

    public EZInventory inventory;

    Set<BlockRef> multiblock = new HashSet<BlockRef>();
    private boolean firstTick = false;
    public boolean hasCraftBox = false;
    public boolean disabled = false;

    public TileEntityStorageCore() {
        inventory = new EZInventory();
    }

    public ItemStack input(ItemStack stack) {
        ItemStack result = this.inventory.input(stack);
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        this.markDirty();
        return result;
    }

    public ItemStack getRandomStack() {
        ItemStack result = this.inventory.getItemsAt(0, 0);
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        this.markDirty();
        return result;
    }

    public void sortInventory() {
        this.inventory.sort();
        updateTileEntity();
    }

    public void updateTileEntity() {
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        this.markDirty();
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, getBlockMetadata(), nbtTag);
    }

    @Override
    public void writeToNBT(NBTTagCompound paramNBTTagCompound) {
        super.writeToNBT(paramNBTTagCompound);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.inventory.slotCount(); ++i) {
            ItemGroup group = this.inventory.inventory.get(i);
            if (group != null && group.itemStack != null && group.count > 0) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Index", (byte) i);
                group.itemStack.writeToNBT(nbttagcompound1);
                nbttagcompound1.setLong("InternalCount", group.count);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        paramNBTTagCompound.setTag("Internal", nbttaglist);
        paramNBTTagCompound.setLong("InternalMax", this.inventory.maxItems);
        paramNBTTagCompound.setBoolean("isDisabled", this.disabled);
    }

    @Override
    public void readFromNBT(NBTTagCompound paramNBTTagCompound) {
        super.readFromNBT(paramNBTTagCompound);
        NBTTagList nbttaglist = paramNBTTagCompound.getTagList("Internal", 10);

        if (nbttaglist != null) {
            inventory.inventory = new ArrayList<ItemGroup>();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                ItemStack stack = ItemStack.loadItemStackFromNBT(nbttagcompound1);
                long count = nbttagcompound1.getLong("InternalCount");
                ItemGroup group = new ItemGroup(stack, count);
                this.inventory.inventory.add(group);
            }
        }
        long maxItems = paramNBTTagCompound.getLong("InternalMax");
        this.inventory.maxItems = maxItems;
        this.disabled = paramNBTTagCompound.getBoolean("isDisabled");
    }

    /**
     * Scans the multiblock structure for valid blocks
     */
    public void scanMultiblock(EntityLivingBase entity) {
        inventory.maxItems = 0;
        this.hasCraftBox = false;
        multiblock = new HashSet<BlockRef>();
        BlockRef ref = new BlockRef(this);
        multiblock.add(ref);
        getValidNeighbors(ref, entity);
        for (BlockRef blockRef : multiblock) {
            if (blockRef.block instanceof BlockStorage sb) {
                inventory.maxItems += sb.getCapacity();
            }
        }
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * Recursive function that scans a block's neighbors, and adds valid blocks to the multiblock list
     * 
     * @param br
     */
    private void getValidNeighbors(BlockRef br, EntityLivingBase entity) {
        List<BlockRef> neighbors = EZStorageUtils.getNeighbors(br.posX, br.posY, br.posZ, worldObj);
        for (BlockRef blockRef : neighbors) {
            if (blockRef.block instanceof StorageMultiblock) {
                if (multiblock.add(blockRef) && validateSystem(entity)) {
                    if (blockRef.block instanceof BlockInputPort) {
                        TileEntity te = worldObj.getTileEntity(blockRef.posX, blockRef.posY, blockRef.posZ);;
                        if (te instanceof TileEntityInventoryProxy teInvProxy) {
                            teInvProxy.core = this;
                        }
                    }
                    if (blockRef.block instanceof BlockCraftingBox) {
                        hasCraftBox = true;
                    }
                    getValidNeighbors(blockRef, entity);
                }
            }
        }
    }

    public boolean validateSystem(EntityLivingBase entity) {
        int count = 0;
        for (BlockRef ref : multiblock) {
            if (ref.block instanceof BlockStorageCore) {
                count++;
            }
            if (count > 1) {
                if (worldObj.isRemote) {
                    if (entity instanceof EntityPlayer entityPlayer) {
                        entityPlayer.addChatComponentMessage(
                            new ChatComponentText("You can only have 1 Storage Core per system!"));
                    }
                } else if (worldObj.getTileEntity(xCoord, yCoord, zCoord)
                    .getBlockType() instanceof BlockStorageCore) {
                        worldObj.setBlockToAir(xCoord, yCoord, zCoord);
                        worldObj.spawnEntityInWorld(
                            new EntityItem(worldObj, xCoord, yCoord, zCoord, new ItemStack(EZBlocks.storage_core)));
                    }
                return false;
            }
        }
        return true;
    }

    public boolean isPartOfMultiblock(BlockRef blockRef) {
        if (multiblock != null) {
            if (multiblock.contains(blockRef)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateEntity() {
        if (!firstTick && worldObj != null) {
            firstTick = true;
            scanMultiblock(null);
        }
    }
}
