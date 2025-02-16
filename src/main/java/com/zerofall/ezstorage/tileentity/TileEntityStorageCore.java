package com.zerofall.ezstorage.tileentity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.EZStorageUtils;

public class TileEntityStorageCore extends TileEntity {

    private EZInventory inventory;

    Set<BlockRef> multiblock = new HashSet<BlockRef>();
    private boolean firstTick = false;
    public boolean hasCraftBox = false;
    public String inventoryId = "";

    public long inventoryItemsStored;
    public long inventoryItemsMax;
    public int inventoryTypesStored;
    public int inventoryTypesMax;

    public EZInventory getInventory() {
        return getInventory(false);
    }

    private EZInventory getInventory(boolean allowCreate) {
        if (inventory == null) {
            inventory = EZInventoryManager.getInventory(inventoryId);

            if (inventory == null && allowCreate) {
                inventory = EZInventoryManager.createInventory();
                inventoryId = inventory.id;
            }
        }
        return inventory;
    }

    public void updateTileEntity(boolean sendInventoryToClients) {
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        this.markDirty();

        if (sendInventoryToClients) {
            EZInventoryManager.sendToClients(inventory, false);
        }
    }

    public void updateTileEntity() {
        updateTileEntity(true);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound nbtTag = pkt.func_148857_g();
        readFromNBT(nbtTag);
        inventoryItemsStored = nbtTag.getLong("inventoryItemsStored");
        inventoryItemsMax = nbtTag.getLong("inventoryItemsMax");
        inventoryTypesStored = nbtTag.getInteger("inventoryTypesStored");
        inventoryTypesMax = nbtTag.getInteger("inventoryTypesMax");
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        EZInventory inventory = getInventory(true);
        if (inventory != null) {
            nbtTag.setLong("inventoryItemsStored", inventory.getTotalCount());
            nbtTag.setLong("inventoryItemsMax", inventory.maxItems);
            nbtTag.setInteger("inventoryTypesStored", inventory.slotCount());
            nbtTag.setInteger("inventoryTypesMax", EZConfiguration.maxItemTypes);
        }
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, getBlockMetadata(), nbtTag);
    }

    @Override
    public void writeToNBT(NBTTagCompound paramNBTTagCompound) {
        super.writeToNBT(paramNBTTagCompound);
        paramNBTTagCompound.setString("inventoryId", inventoryId);
    }

    @Override
    public void readFromNBT(NBTTagCompound paramNBTTagCompound) {
        super.readFromNBT(paramNBTTagCompound);
        inventoryId = paramNBTTagCompound.getString("inventoryId");

        // Migrate old data that was saved to the TE
        if (paramNBTTagCompound.hasKey("Internal")) {
            EZInventory inventory = new EZInventory();
            inventory.readFromNBT(paramNBTTagCompound);
            EZInventoryManager.createInventory(inventory);
            inventoryId = inventory.id;
        }
    }

    /**
     * Scans the multiblock structure for valid blocks
     */
    public void scanMultiblock(EntityLivingBase entity) {
        EZInventory inventory = getInventory(true);
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
            if (!worldObj.isRemote) {
                scanMultiblock(null);
                EZInventoryManager.sendToClients(getInventory());
            }
        }
    }
}
