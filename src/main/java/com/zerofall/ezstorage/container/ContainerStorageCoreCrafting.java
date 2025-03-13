package com.zerofall.ezstorage.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;

import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

public class ContainerStorageCoreCrafting extends ContainerStorageCore {

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public IInventory craftResult = new InventoryCraftResult();
    private World worldObj;

    public ContainerStorageCoreCrafting(EntityPlayer player, World world, EZInventory inventory) {
        this(player, world);
        this.inventory = inventory;
    }

    public ContainerStorageCoreCrafting(EntityPlayer player, World world) {
        super(player);
        this.worldObj = world;
        this.addSlotToContainer(new SlotCrafting(player, this.craftMatrix, this.craftResult, 0, 116, 132));
        int i;
        int j;

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 44 + j * 18, 114 + i * 18));
            }
        }
        this.onCraftMatrixChanged(this.craftMatrix);
    }

    public void onCraftMatrixChanged(IInventory inventoryIn) {
        this.craftResult.setInventorySlotContents(
            0,
            CraftingManager.getInstance()
                .findMatchingRecipe(this.craftMatrix, this.worldObj));
    }

    // Shift clicking
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slotObject = (Slot) inventorySlots.get(index);
        if (slotObject != null && slotObject.getHasStack()) {
            if (slotObject instanceof SlotCrafting) {
                boolean hasChanges = false;
                ItemStack[][] recipe = new ItemStack[9][];
                for (int i = 0; i < 9; i++) {
                    recipe[i] = new ItemStack[] { this.craftMatrix.getStackInSlot(i) };
                }

                ItemStack slotStack = slotObject.getStack();
                ItemStack resultStack = null;
                ItemStack original = slotStack.copy();
                int crafted = 0;
                int maxStackSize = slotStack.getMaxStackSize();
                int crafting = slotStack.stackSize;

                while (true) {
                    if (!slotObject.getHasStack() || !slotObject.getStack()
                        .isItemEqual(slotStack)) {
                        break;
                    }
                    if (crafting >= maxStackSize) {
                        break;
                    }

                    slotStack = slotObject.getStack();
                    if (crafted + slotStack.stackSize > slotStack.getMaxStackSize()) {
                        break;
                    }

                    resultStack = slotStack.copy();
                    boolean merged = this
                        .mergeItemStack(slotStack, this.rowCount() * 9, this.rowCount() * 9 + 36, true);
                    if (!merged) {
                        return null;
                    }

                    // It merged! grab another
                    crafted += resultStack.stackSize;
                    slotObject.onSlotChange(slotStack, resultStack);
                    slotObject.onPickupFromSlot(playerIn, slotStack);

                    if (slotObject.getStack() == null || !original.isItemEqual(slotObject.getStack())) {
                        if (tryToPopulateCraftingGrid(recipe, playerIn, false)) {
                            hasChanges = true;
                        }
                    }
                }

                if (hasChanges) {
                    EZInventoryManager.sendToClients(inventory);
                }

                if (resultStack == null || slotStack.stackSize == resultStack.stackSize) {
                    return null;
                }

                return resultStack;
            } else {
                ItemStack stackInSlot = slotObject.getStack();
                slotObject.putStack(this.inventory.input(stackInSlot));
                EZInventoryManager.sendToClients(inventory);
            }
        }
        return null;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        if (slotId > 0 && mode == 0 && clickedButton == 0) {
            if (slotId >= 0 && inventorySlots.size() > slotId) {
                Slot slotObject = inventorySlots.get(slotId);
                if (slotObject != null) {
                    if (slotObject instanceof SlotCrafting) {
                        ItemStack[][] recipe = new ItemStack[9][];
                        for (int i = 0; i < 9; i++) {
                            recipe[i] = new ItemStack[] { this.craftMatrix.getStackInSlot(i) };
                        }
                        ItemStack result = super.slotClick(slotId, clickedButton, mode, playerIn);
                        if (result != null && tryToPopulateCraftingGrid(recipe, playerIn, false)) {
                            EZInventoryManager.sendToClients(inventory);
                        }
                        return result;
                    }
                }
            }

        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    public boolean tryToPopulateCraftingGrid(ItemStack[][] recipe, EntityPlayer playerIn, boolean usePlayerInv) {
        boolean hasChanges = false;
        HashMap<Integer, ArrayList<Slot>> playerInvSlotsMapping = new HashMap<>();

        for (int j = 0; j < recipe.length; j++) {
            ItemStack[] recipeItems = recipe[j];

            Slot slot = getSlotFromInventory(this.craftMatrix, j);
            if (slot == null) {
                continue;
            }

            ItemStack stackInSlot = slot.getStack();
            if (stackInSlot != null) {
                if (recipeItems == null || recipeItems.length == 0
                    || getMatchingItemStackForRecipe(recipeItems, stackInSlot) == null) {
                    ItemStack result = this.inventory.input(stackInSlot);
                    this.craftMatrix.setInventorySlotContents(j, null);
                    if (result == null) {
                        playerIn.dropPlayerItemWithRandomChoice(result, false);
                    }
                    hasChanges = true;
                } else {
                    continue;
                }
            }

            if (recipeItems == null || recipeItems.length == 0) {
                continue;
            }

            ItemStack retreived = null;

            for (int k = 0; k < recipeItems.length; k++) {
                ItemStack recipeItem = recipeItems[k];

                if (recipeItem == null) {
                    continue;
                }

                if (recipeItem.stackSize > 1) {
                    continue;
                } else {
                    recipeItem.stackSize = 1;
                }

                boolean foundInPlayerInv = false;
                if (usePlayerInv) {
                    Integer playerInvSize = playerIn.inventory.mainInventory.length;
                    for (int i = 0; i < playerInvSize; i++) {
                        ItemStack playerItem = playerIn.inventory.mainInventory[i];
                        if (playerItem != null && playerItem.isItemEqual(recipeItem)) {
                            ArrayList<Slot> targetSlots = playerInvSlotsMapping.get(i);
                            if (targetSlots == null) {
                                targetSlots = new ArrayList<>();
                                playerInvSlotsMapping.put(i, targetSlots);
                            }
                            if (playerItem.stackSize > targetSlots.size()) {
                                targetSlots.add(slot);
                                foundInPlayerInv = true;
                                break;
                            }
                        }
                    }
                }

                if (retreived == null && !foundInPlayerInv) {
                    retreived = inventory.getItems(new ItemStack[] { recipeItem });
                    if (retreived != null) {
                        hasChanges = true;
                        break;
                    }
                }
            }

            if (retreived != null) {
                slot.putStack(retreived);
            }
        }

        if (usePlayerInv && playerInvSlotsMapping.size() != 0) {
            Set<Entry<Integer, ArrayList<Slot>>> set = playerInvSlotsMapping.entrySet();
            for (Entry<Integer, ArrayList<Slot>> entry : set) {
                Integer playerInvSlotId = entry.getKey();
                ArrayList<Slot> targetSlots = entry.getValue();
                int targetSlotsCount = targetSlots.size();
                ItemStack playerInvSlot = playerIn.inventory.mainInventory[playerInvSlotId];
                if (playerInvSlot == null) {
                    continue;
                }
                int itemsToRequest = (int) (playerInvSlot.stackSize / targetSlotsCount);

                for (int j = 0; j < targetSlotsCount; j++) {
                    Slot targetSlot = targetSlots.get(j);
                    if (targetSlot == null) {
                        continue;
                    }
                    if (j == targetSlotsCount - 1) {
                        playerInvSlot = playerIn.inventory.mainInventory[playerInvSlotId];
                        if (playerInvSlot == null) {
                            break;
                        }
                        itemsToRequest = playerInvSlot.stackSize;
                    }
                    ItemStack retrived = playerIn.inventory.decrStackSize(playerInvSlotId, itemsToRequest);
                    if (retrived != null) {
                        targetSlot.putStack(retrived);
                    }
                }
            }
        }

        return hasChanges;
    }

    private static ItemStack getMatchingItemStackForRecipe(ItemStack[] recipeItems, ItemStack stack) {
        for (ItemStack recipeItem : recipeItems) {
            if (recipeItem.getItem() == stack.getItem()
                && (recipeItem.getItemDamage() == stack.getItemDamage() || stack.isItemStackDamageable())) {
                return recipeItem;
            }
        }
        return null;
    }

    @Override
    protected int playerInventoryY() {
        return 174;
    }

    @Override
    protected int rowCount() {
        return 5;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        clearGrid(playerIn);
        super.onContainerClosed(playerIn);
    }

    public void clearGrid(EntityPlayer playerIn) {
        boolean cleared = false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.craftMatrix.getStackInSlot(i);
            if (stack != null) {
                ItemStack result = this.inventory.input(stack);
                this.craftMatrix.setInventorySlotContents(i, null);
                if (result != null) {
                    playerIn.dropPlayerItemWithRandomChoice(result, false);
                }
                cleared = true;
            }
        }

        if (cleared && !playerIn.worldObj.isRemote) {
            EZInventoryManager.sendToClients(inventory);
        }
    }
}
