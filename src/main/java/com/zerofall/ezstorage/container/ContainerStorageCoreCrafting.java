package com.zerofall.ezstorage.container;

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
                ItemStack[] recipe = new ItemStack[9];
                for (int i = 0; i < 9; i++) {
                    recipe[i] = this.craftMatrix.getStackInSlot(i);
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
                        tryToPopulateCraftingGrid(recipe, playerIn);
                    }
                }

                if (resultStack == null || slotStack.stackSize == resultStack.stackSize) {
                    return null;
                }

                return resultStack;
            }

            ItemStack stackInSlot = slotObject.getStack();
            slotObject.putStack(this.inventory.input(stackInSlot));
            EZInventoryManager.sendToClients(inventory);
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
                        ItemStack[] recipe = new ItemStack[9];
                        for (int i = 0; i < 9; i++) {
                            recipe[i] = this.craftMatrix.getStackInSlot(i);
                        }
                        ItemStack result = super.slotClick(slotId, clickedButton, mode, playerIn);
                        if (result != null) {
                            tryToPopulateCraftingGrid(recipe, playerIn);
                        }
                        return result;
                    }
                }
            }

        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    private void tryToPopulateCraftingGrid(ItemStack[] recipe, EntityPlayer playerIn) {
        clearGrid(playerIn);
        for (int j = 0; j < recipe.length; j++) {
            if (recipe[j] != null) {
                if (recipe[j].stackSize > 1) {
                    continue;
                } else {
                    recipe[j].stackSize = 1;
                }
                Slot slot = getSlotFromInventory(this.craftMatrix, j);
                if (slot != null) {
                    ItemStack retreived = inventory.getItems(new ItemStack[] { recipe[j] });
                    if (retreived != null) {
                        slot.putStack(retreived);
                    }
                }
            }
        }
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

        if (cleared) {
            EZInventoryManager.sendToClients(inventory);
        }
    }
}
