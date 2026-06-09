package com.zerofall.ezstorage.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import com.zerofall.ezstorage.block.BlockCraftingBox;
import com.zerofall.ezstorage.block.BlockInventoryProxy;
import com.zerofall.ezstorage.block.BlockStorage;
import com.zerofall.ezstorage.block.BlockStorageAdapter;
import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.block.StorageMultiblock;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.storage.ExternalStorageProvider;
import com.zerofall.ezstorage.storage.IStorageProvider;
import com.zerofall.ezstorage.storage.InternalStorageProvider;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.EZStorageUtils;

public class TileEntityStorageCore extends TileEntity {

    private EZInventory inventory;

    Set<BlockRef> multiblock = new HashSet<BlockRef>();
    private int ticks;
    public boolean hasCraftBox = false;
    public String inventoryId = "";

    public long inventoryItemsStored;
    public long inventoryItemsMax;
    public int inventoryTypesStored;
    public int inventoryTypesMax;

    private List<IStorageProvider> providers = new ArrayList<IStorageProvider>();
    private long lastExternalStateHash = 0;
    private int externalSyncTicks = 0;

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
            EZInventoryManager.sendToClients(inventory, this);
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
        scanMultiblock(entity, true);
    }

    /**
     * Scans the multiblock structure for valid blocks
     */
    public void scanMultiblock(EntityLivingBase entity, boolean force) {
        EZInventory inventory = getInventory(true);
        int maxItems = 0;
        inventory.maxItems = 0;
        this.hasCraftBox = false;
        multiblock = new HashSet<BlockRef>();
        initProviders();
        BlockRef ref = new BlockRef(this);
        multiblock.add(ref);
        getValidNeighbors(ref, entity);
        for (BlockRef blockRef : multiblock) {
            if (blockRef.block instanceof BlockStorage sb) {
                maxItems += sb.getCapacity();
            }
        }
        if (!force && inventory.maxItems == maxItems) {
            return;
        }
        inventory.maxItems = maxItems;
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * Notifies all blocks of the multiblock to validate their connection to the storage core.
     */
    public void notifyNeighbor(BlockRef blockRef) {
        worldObj.notifyBlockOfNeighborChange(blockRef.posX, blockRef.posY, blockRef.posZ, blockRef.block);
    }

    /**
     * Recursive function that scans a block's neighbors, and adds valid blocks to the multiblock list
     *
     * @param br
     */
    private void getValidNeighbors(BlockRef br, EntityLivingBase entity) {
        List<BlockRef> neighbors = EZStorageUtils.getNeighbors(br.posX, br.posY, br.posZ, worldObj);
        for (BlockRef blockRef : neighbors) {
            if (blockRef.block instanceof BlockStorageAdapter) {
                TileEntity te = worldObj.getTileEntity(blockRef.posX, blockRef.posY, blockRef.posZ);
                if (te instanceof TileEntityStorageAdapter) {
                    TileEntityStorageAdapter adapter = (TileEntityStorageAdapter) te;
                    adapter.setCore(this);
                    adapter.scanConnectedInventories();
                    for (BlockRef extRef : adapter.getConnectedInventories()) {
                        addExternalProvider(
                            new ExternalStorageProvider(worldObj, extRef.posX, extRef.posY, extRef.posZ));
                    }
                }
            }
            if (blockRef.block instanceof StorageMultiblock && multiblock.add(blockRef) && validateSystem(entity)) {
                if (blockRef.block instanceof BlockInventoryProxy) {
                    TileEntity te = worldObj.getTileEntity(blockRef.posX, blockRef.posY, blockRef.posZ);;
                    if (te instanceof TileEntityMultiblock teInvProxy) {
                        teInvProxy.setCore(this);
                    }
                }
                if (blockRef.block instanceof BlockCraftingBox) {
                    hasCraftBox = true;
                }
                getValidNeighbors(blockRef, entity);
            }
        }
    }

    public boolean validateSystem(EntityLivingBase entity) {
        int count = 0;
        for (BlockRef ref : multiblock) {
            if (ref.block instanceof BlockStorageCore) {
                count++;
            }
            if (count > 1 && worldObj.getBlock(xCoord, yCoord, zCoord) instanceof BlockStorageCore) {
                worldObj.setBlockToAir(xCoord, yCoord, zCoord);
                worldObj.spawnEntityInWorld(
                    new EntityItem(worldObj, xCoord, yCoord, zCoord, new ItemStack(EZBlocks.storage_core)));
                return false;
            }
        }
        return true;
    }

    /** Initialize providers list - called at start of scanMultiblock */
    private void initProviders() {
        providers.clear();
        EZInventory inv = getInventory();
        if (inv != null) {
            providers.add(new InternalStorageProvider(inv));
        }
    }

    /** Get unmodifiable list of providers */
    public List<IStorageProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    /** Get the internal (first) provider */
    public InternalStorageProvider getInternalProvider() {
        if (providers.isEmpty()) {
            return null;
        }
        return (InternalStorageProvider) providers.get(0);
    }

    /** Add an external storage provider */
    public void addExternalProvider(IStorageProvider provider) {
        if (provider != null) {
            providers.add(provider);
        }
    }

    /** Clear all external providers (keeps only internal) */
    public void clearExternalProviders() {
        if (providers.size() > 1) {
            IStorageProvider internal = providers.get(0);
            providers.clear();
            providers.add(internal);
        }
    }

    /**
     * Merged and deduplicated item list across all providers.
     * Rebuilds from scratch each call (no caching).
     */
    public List<ItemStack> getUnifiedItemList() {
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (IStorageProvider provider : providers) {
            if (!provider.isValid()) continue;
            List<ItemStack> items = provider.getAllItems();
            for (ItemStack item : items) {
                if (item == null) continue;
                boolean merged = false;
                for (ItemStack existing : result) {
                    if (EZInventory.stacksEqual(existing, item)) {
                        existing.stackSize += item.stackSize;
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    result.add(item.copy());
                }
            }
        }
        return result;
    }

    public long getUnifiedTotalCount() {
        long total = 0;
        for (IStorageProvider provider : providers) {
            if (provider.isValid()) {
                total += provider.getTotalCount();
            }
        }
        return total;
    }

    public long getUnifiedCapacity() {
        long total = 0;
        for (IStorageProvider provider : providers) {
            if (provider.isValid()) {
                total += provider.getCapacity();
            }
        }
        return total;
    }

    public int getUnifiedSlotCount() {
        return getUnifiedItemList().size();
    }

    /**
     * Input items through the unified interface.
     * Two-pass strategy: providers with existing matching items first (consolidation),
     * then remaining providers.
     */
    public ItemStack unifiedInput(ItemStack stack) {
        if (stack == null) return null;
        ItemStack remainder = stack;

        // Pass 1: providers that already have matching items
        for (IStorageProvider provider : providers) {
            if (!provider.isValid() || remainder == null) continue;
            if (providerHasMatchingItem(provider, remainder)) {
                remainder = provider.input(remainder);
            }
        }

        // Pass 2: remaining providers
        for (IStorageProvider provider : providers) {
            if (!provider.isValid() || remainder == null) continue;
            if (!providerHasMatchingItem(provider, remainder)) {
                remainder = provider.input(remainder);
            }
        }

        return remainder;
    }

    private boolean providerHasMatchingItem(IStorageProvider provider, ItemStack stack) {
        for (ItemStack item : provider.getAllItems()) {
            if (item != null && EZInventory.stacksEqual(item, stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract items from the unified view.
     * Looks up the target item by unifiedIndex, then extracts across all providers
     * that have matching items until the requested amount is fulfilled.
     */
    public ItemStack unifiedExtract(int unifiedIndex, int type) {
        if (unifiedIndex < 0) return null;

        List<ItemStack> unified = getUnifiedItemList();
        if (unifiedIndex >= unified.size()) return null;

        ItemStack target = unified.get(unifiedIndex);
        int maxStackSize = target.getMaxStackSize();
        int totalAvailable = target.stackSize;

        int toExtract;
        if (type == 0) {
            toExtract = Math.min(maxStackSize, totalAvailable);
        } else if (type == 1) {
            toExtract = Math.min(maxStackSize, totalAvailable) / 2;
        } else {
            toExtract = 1;
        }
        if (toExtract <= 0) return null;

        ItemStack result = target.copy();
        result.stackSize = 0;
        int remaining = toExtract;

        for (IStorageProvider provider : providers) {
            if (!provider.isValid() || remaining <= 0) continue;
            int localIndex = findItemIndex(provider, target);
            if (localIndex < 0) continue;

            ItemStack extracted = provider.extractExact(localIndex, remaining);
            if (extracted != null && extracted.stackSize > 0) {
                result.stackSize += extracted.stackSize;
                remaining -= extracted.stackSize;
            }
        }

        return result.stackSize > 0 ? result : null;
    }

    private int findItemIndex(IStorageProvider provider, ItemStack target) {
        List<ItemStack> items = provider.getAllItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null && EZInventory.stacksEqual(items.get(i), target)) {
                return i;
            }
        }
        return -1;
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
        // First scan
        if (ticks == 0 && worldObj != null && !worldObj.isRemote) {
            scanMultiblock(null);
        }

        // 400 ticks = 20 ticks * 20 seconds
        if (ticks >= 400) {

            // Periodical scan
            if (!worldObj.isRemote) {
                scanMultiblock(null, false);
            }

            // Reset
            ticks = 0;
        }

        // Periodic external container resync (every 40 ticks = 2 seconds)
        if (!worldObj.isRemote && providers.size() > 1) {
            externalSyncTicks++;
            if (externalSyncTicks >= 40) {
                externalSyncTicks = 0;
                boolean needsRescan = false;
                long hash = 0;
                for (int i = 1; i < providers.size(); i++) {
                    IStorageProvider provider = providers.get(i);
                    if (!provider.isValid()) {
                        needsRescan = true;
                        break;
                    }
                    hash = hash * 31 + provider.getTotalCount() + provider.getSlotCount();
                }
                if (needsRescan) {
                    scanMultiblock(null, false);
                } else if (hash != lastExternalStateHash) {
                    lastExternalStateHash = hash;
                    updateTileEntity();
                }
            }
        }

        // Increment
        ticks += 1;
    }
}
