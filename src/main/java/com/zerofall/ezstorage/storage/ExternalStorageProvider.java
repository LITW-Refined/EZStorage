package com.zerofall.ezstorage.storage;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import com.zerofall.ezstorage.util.EZInventory;

public class ExternalStorageProvider implements IStorageProvider {

    private final World world;
    private final int x, y, z;

    // Rebuilt each time getAllItems() is called
    private List<ItemStack> mergedList = new ArrayList<ItemStack>();
    private List<List<Integer>> slotMapping = new ArrayList<List<Integer>>();

    public ExternalStorageProvider(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    private IInventory getInventory() {
        if (world == null) return null;
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof IInventory)) return null;

        if (te instanceof TileEntityChest chest) {
            Block chestBlock = world.getBlock(x, y, z);
            int[][] horizontalDirs = { { -1, 0, 0 }, { 1, 0, 0 }, { 0, 0, -1 }, { 0, 0, 1 } };
            for (int[] dir : horizontalDirs) {
                int nx = x + dir[0], nz = z + dir[2];
                if (world.getBlock(nx, y, nz) == chestBlock) {
                    TileEntity adjTe = world.getTileEntity(nx, y, nz);
                    if (adjTe instanceof TileEntityChest adjChest) {
                        if (dir[0] == -1 || dir[2] == -1) {
                            return new InventoryLargeChest("Large Chest", adjChest, chest);
                        } else {
                            return new InventoryLargeChest("Large Chest", chest, adjChest);
                        }
                    }
                }
            }
        }

        return (IInventory) te;
    }

    private int[] getAccessibleSlots(IInventory inv) {
        if (inv instanceof ISidedInventory) {
            // Try all 6 sides, use the first one that returns slots
            for (int side = 0; side < 6; side++) {
                int[] slots = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side);
                if (slots != null && slots.length > 0) return slots;
            }
        }
        // Fallback: all slots
        int[] all = new int[inv.getSizeInventory()];
        for (int i = 0; i < all.length; i++) all[i] = i;
        return all;
    }

    private boolean canInsert(IInventory inv, int slot, ItemStack stack) {
        if (inv instanceof ISidedInventory) {
            // Check any side that includes this slot
            for (int side = 0; side < 6; side++) {
                int[] accessible = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side);
                for (int s : accessible) {
                    if (s == slot) {
                        return ((ISidedInventory) inv).canInsertItem(slot, stack, side);
                    }
                }
            }
            return false;
        }
        return inv.isItemValidForSlot(slot, stack);
    }

    private boolean canExtract(IInventory inv, int slot, ItemStack stack) {
        if (inv instanceof ISidedInventory) {
            for (int side = 0; side < 6; side++) {
                int[] accessible = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side);
                for (int s : accessible) {
                    if (s == slot) {
                        return ((ISidedInventory) inv).canExtractItem(slot, stack, side);
                    }
                }
            }
            return false;
        }
        return true;
    }

    private void rebuildMergedView() {
        mergedList = new ArrayList<ItemStack>();
        slotMapping = new ArrayList<List<Integer>>();
        IInventory inv = getInventory();
        if (inv == null) return;

        int[] slots = getAccessibleSlots(inv);
        for (int slot : slots) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;

            boolean merged = false;
            for (int i = 0; i < mergedList.size(); i++) {
                if (EZInventory.stacksEqual(mergedList.get(i), stack)) {
                    mergedList.get(i).stackSize += stack.stackSize;
                    slotMapping.get(i)
                        .add(slot);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                ItemStack copy = stack.copy();
                mergedList.add(copy);
                List<Integer> mapping = new ArrayList<Integer>();
                mapping.add(slot);
                slotMapping.add(mapping);
            }
        }
    }

    @Override
    public boolean isValid() {
        try {
            return getInventory() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ItemStack> getAllItems() {
        try {
            rebuildMergedView();
            return mergedList;
        } catch (Exception e) {
            return new ArrayList<ItemStack>();
        }
    }

    @Override
    public long getTotalCount() {
        try {
            rebuildMergedView();
            long count = 0;
            for (ItemStack stack : mergedList) {
                count += stack.stackSize;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public long getCapacity() {
        try {
            IInventory inv = getInventory();
            if (inv == null) return 0;
            return (long) inv.getSizeInventory() * inv.getInventoryStackLimit();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getSlotCount() {
        try {
            rebuildMergedView();
            return mergedList.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public ItemStack input(ItemStack itemStack) {
        try {
            IInventory inv = getInventory();
            if (inv == null || itemStack == null) return itemStack;

            ItemStack remainder = itemStack.copy();
            int[] slots = getAccessibleSlots(inv);

            int stackLimit = Math.min(inv.getInventoryStackLimit(), itemStack.getMaxStackSize());

            // First pass: stack onto existing matching slots
            for (int slot : slots) {
                if (remainder == null || remainder.stackSize <= 0) break;
                ItemStack existing = inv.getStackInSlot(slot);
                if (existing != null && EZInventory.stacksEqual(existing, remainder)) {
                    if (!canInsert(inv, slot, remainder)) continue;
                    int space = stackLimit - existing.stackSize;
                    if (space <= 0) continue;
                    int toAdd = Math.min(space, remainder.stackSize);
                    existing.stackSize += toAdd;
                    remainder.stackSize -= toAdd;
                    inv.setInventorySlotContents(slot, existing);
                    if (remainder.stackSize <= 0) remainder = null;
                }
            }

            // Second pass: find empty slots
            for (int slot : slots) {
                if (remainder == null || remainder.stackSize <= 0) break;
                ItemStack existing = inv.getStackInSlot(slot);
                if (existing != null) continue;
                if (!canInsert(inv, slot, remainder)) continue;
                int toPlace = Math.min(stackLimit, remainder.stackSize);
                ItemStack toSet = remainder.copy();
                toSet.stackSize = toPlace;
                inv.setInventorySlotContents(slot, toSet);
                remainder.stackSize -= toPlace;
                if (remainder.stackSize <= 0) remainder = null;
            }

            inv.markDirty();
            return remainder;
        } catch (Exception e) {
            return itemStack;
        }
    }

    @Override
    public ItemStack simulateInput(ItemStack itemStack) {
        try {
            IInventory inv = getInventory();
            if (inv == null || itemStack == null) return itemStack;

            ItemStack remainder = itemStack.copy();
            int[] slots = getAccessibleSlots(inv);
            int stackLimit = Math.min(inv.getInventoryStackLimit(), itemStack.getMaxStackSize());

            for (int slot : slots) {
                if (remainder == null || remainder.stackSize <= 0) break;
                ItemStack existing = inv.getStackInSlot(slot);
                if (existing != null && EZInventory.stacksEqual(existing, remainder)) {
                    if (!canInsert(inv, slot, remainder)) continue;
                    int space = stackLimit - existing.stackSize;
                    if (space <= 0) continue;
                    int toAdd = Math.min(space, remainder.stackSize);
                    remainder.stackSize -= toAdd;
                    if (remainder.stackSize <= 0) remainder = null;
                }
            }

            for (int slot : slots) {
                if (remainder == null || remainder.stackSize <= 0) break;
                ItemStack existing = inv.getStackInSlot(slot);
                if (existing != null) continue;
                if (!canInsert(inv, slot, remainder)) continue;
                int toPlace = Math.min(stackLimit, remainder.stackSize);
                remainder.stackSize -= toPlace;
                if (remainder.stackSize <= 0) remainder = null;
            }

            return remainder;
        } catch (Exception e) {
            return itemStack;
        }
    }

    @Override
    public ItemStack extract(int index, int type) {
        try {
            rebuildMergedView();
            if (index < 0 || index >= mergedList.size()) return null;

            ItemStack merged = mergedList.get(index);
            int total = merged.stackSize;
            int maxStackSize = merged.getMaxStackSize();
            int toExtract;
            if (type == 0) {
                toExtract = Math.min(maxStackSize, total);
            } else if (type == 1) {
                toExtract = Math.min(maxStackSize, total) / 2;
            } else {
                toExtract = 1;
            }
            if (toExtract <= 0) return null;
            return extractExact(index, toExtract);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ItemStack extractAll(int index) {
        try {
            rebuildMergedView();
            if (index < 0 || index >= mergedList.size()) return null;

            ItemStack merged = mergedList.get(index);
            List<Integer> sources = slotMapping.get(index);

            IInventory inv = getInventory();
            if (inv == null) return null;

            ItemStack result = merged.copy();

            for (int slot : sources) {
                ItemStack slotStack = inv.getStackInSlot(slot);
                if (slotStack == null || !EZInventory.stacksEqual(slotStack, merged)) continue;
                if (!canExtract(inv, slot, slotStack)) continue;
                inv.setInventorySlotContents(slot, null);
            }

            inv.markDirty();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ItemStack extractExact(int index, int amount) {
        try {
            rebuildMergedView();
            if (index < 0 || index >= mergedList.size()) return null;
            if (amount <= 0) return null;

            ItemStack merged = mergedList.get(index);
            List<Integer> sources = slotMapping.get(index);
            int toExtract = Math.min(amount, merged.stackSize);
            if (toExtract <= 0) return null;

            IInventory inv = getInventory();
            if (inv == null) return null;

            ItemStack result = merged.copy();
            result.stackSize = 0;
            int remaining = toExtract;

            for (int slot : sources) {
                if (remaining <= 0) break;
                ItemStack slotStack = inv.getStackInSlot(slot);
                if (slotStack == null || !EZInventory.stacksEqual(slotStack, merged)) continue;
                if (!canExtract(inv, slot, slotStack)) continue;

                int taken = Math.min(remaining, slotStack.stackSize);
                slotStack.stackSize -= taken;
                remaining -= taken;
                result.stackSize += taken;

                if (slotStack.stackSize <= 0) {
                    inv.setInventorySlotContents(slot, null);
                } else {
                    inv.setInventorySlotContents(slot, slotStack);
                }
            }

            inv.markDirty();
            return result.stackSize > 0 ? result : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void markDirty() {
        try {
            IInventory inv = getInventory();
            if (inv != null) inv.markDirty();
        } catch (Exception e) {
            // silent
        }
    }
}
