package com.zerofall.ezstorage.nei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.gui.GuiCraftingCore;
import com.zerofall.ezstorage.gui.GuiStorageCore;
import com.zerofall.ezstorage.network.client.MsgCraftRecipe;
import com.zerofall.ezstorage.network.client.MsgReqCrafting;
import com.zerofall.ezstorage.util.EZInventory;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton.ItemOverlayState;
import codechicken.nei.recipe.IRecipeHandler;

public class NeiCraftingOverlay implements IOverlayHandler {

    @Override
    public void overlayRecipe(final GuiContainer gui, final IRecipeHandler recipe, final int recipeIndex,
        final boolean maxTransfer) {
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
        overlayRecipe(gui, ingredients);
    }

    public void overlayRecipe(final GuiContainer gui, final List<PositionedStack> ingredients) {
        if (!(gui instanceof GuiCraftingCore)) {
            return;
        }
        EZStorage.instance.network.sendToServer(new MsgReqCrafting(encodeRecipe(ingredients)));
    }

    // Must be overridden (non-default) or NEI won't offer canCraft()/craft() for this handler at all.
    @Override
    public int transferRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        overlayRecipe(firstGui, recipe, recipeIndex, multiplier != 1);
        return 1;
    }

    @Override
    public boolean canCraft(GuiContainer firstGui, IRecipeHandler handler, int recipeIndex) {
        return presenceOverlay(firstGui, handler, recipeIndex).stream()
            .allMatch(ItemOverlayState::isPresent);
    }

    // Predicts the craft locally instead of filling the grid and waiting on a slot-sync round trip; server replicates
    // it via MsgCraftRecipe.
    @Override
    public boolean craft(GuiContainer firstGui, IRecipeHandler handler, int recipeIndex, int multiplier) {
        if (!(firstGui instanceof GuiCraftingCore gui)) {
            return false;
        }
        EZInventory inventory = gui.getInventory();
        if (inventory == null) {
            return false;
        }

        List<PositionedStack> ingredients = handler.getIngredientStacks(recipeIndex);
        InventoryPlayer playerInv = gui.mc.thePlayer.inventory;

        int crafts = maxCraftable(inventory, playerInv.mainInventory, ingredients, multiplier);
        if (crafts <= 0) {
            return false;
        }

        deductIngredients(inventory, playerInv, ingredients, crafts);

        PositionedStack resultStack = handler.getResultStack(recipeIndex);
        if (resultStack != null && resultStack.items != null && resultStack.items.length > 0) {
            ItemStack perCraft = resultStack.items[0];
            ItemStack result = perCraft.copy();
            result.stackSize = Math.max(1, perCraft.stackSize) * crafts;
            inventory.input(result);
        }

        EZStorage.instance.network.sendToServer(new MsgCraftRecipe(encodeRecipe(ingredients), crafts));
        return true;
    }

    // Simulates against copies, doesn't touch the real inventory/storage.
    private int maxCraftable(EZInventory inventory, ItemStack[] playerInv, List<PositionedStack> ingredients,
        int limit) {
        List<ItemStack> pool = new ArrayList<>(inventory.inventory.size() + playerInv.length);
        synchronized (inventory.inventory) {
            for (ItemStack stack : inventory.inventory) {
                pool.add(stack.copy());
            }
        }
        for (ItemStack stack : playerInv) {
            if (stack != null && stack.stackSize > 0) {
                pool.add(stack.copy());
            }
        }

        for (int crafts = 0; crafts < limit; crafts++) {
            for (PositionedStack ingredient : ingredients) {
                if (ingredient == null || ingredient.items == null || ingredient.items.length == 0) {
                    continue;
                }
                if (!consumeOne(pool, ingredient)) {
                    return crafts;
                }
            }
        }
        return limit;
    }

    private boolean consumeOne(List<ItemStack> pool, PositionedStack ingredient) {
        for (ItemStack alternative : ingredient.items) {
            if (alternative == null) {
                continue;
            }
            int needed = alternative.stackSize > 0 ? alternative.stackSize : 1;
            for (ItemStack candidate : pool) {
                if (candidate.stackSize >= needed
                    && ContainerStorageCoreCrafting.isRecipeItemValid(alternative, candidate)) {
                    ItemStack used = candidate.copy();
                    used.stackSize = needed;
                    candidate.stackSize -= needed;
                    ItemStack toReturn = ContainerStorageCoreCrafting.afterCraftingUse(used);
                    if (toReturn != null) {
                        pool.add(toReturn);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void deductIngredients(EZInventory inventory, InventoryPlayer playerInv, List<PositionedStack> ingredients,
        int crafts) {
        for (int i = 0; i < crafts; i++) {
            for (PositionedStack ingredient : ingredients) {
                if (ingredient == null || ingredient.items == null || ingredient.items.length == 0) {
                    continue;
                }
                for (ItemStack alternative : ingredient.items) {
                    if (alternative == null) {
                        continue;
                    }
                    int needed = alternative.stackSize > 0 ? alternative.stackSize : 1;
                    ItemStack taken;
                    synchronized (inventory.inventory) {
                        taken = ContainerStorageCoreCrafting.getMatchingItemFromStorage(inventory, alternative, needed);
                        if (taken != null) {
                            returnByproduct(inventory, taken);
                        }
                    }
                    if (taken == null) {
                        taken = ContainerStorageCoreCrafting.takeFromPlayerInventory(playerInv, alternative, needed);
                        if (taken != null) {
                            returnByproduct(inventory, taken);
                        }
                    }
                    if (taken != null) {
                        break;
                    }
                }
            }
        }
    }

    private void returnByproduct(EZInventory inventory, ItemStack used) {
        ItemStack toReturn = ContainerStorageCoreCrafting.afterCraftingUse(used);
        if (toReturn != null) {
            inventory.input(toReturn);
        }
    }

    private NBTTagCompound encodeRecipe(List<PositionedStack> ingredients) {
        final NBTTagCompound recipe = new NBTTagCompound();

        for (final PositionedStack positionedStack : ingredients) {
            if (positionedStack == null || positionedStack.items == null || positionedStack.items.length == 0) {
                continue;
            }

            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;
            final int craftMatrixIndex = col + row * 3;

            final NBTTagList tags = new NBTTagList();
            for (final ItemStack is : positionedStack.items) {
                final NBTTagCompound tag = new NBTTagCompound();
                is.writeToNBT(tag);
                tags.appendTag(tag);
            }

            recipe.setTag("#" + craftMatrixIndex, tags);
        }

        return recipe;
    }

    @Override
    public List<ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        List<ItemStack> invStacks = new ArrayList<ItemStack>();

        if (firstGui instanceof GuiStorageCore coreGui) {
            invStacks.addAll(coreGui.copyStorageStacks());
        }

        invStacks
            .addAll(getFromInventory(firstGui.mc.thePlayer.inventoryContainer.inventorySlots, firstGui.mc.thePlayer));

        final List<ItemOverlayState> itemPresenceSlots = new ArrayList<>();
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);

        for (PositionedStack stack : ingredients) {
            Optional<ItemStack> used = invStacks.stream()
                .filter(is -> is.stackSize > 0 && stack.contains(is))
                .findAny();

            itemPresenceSlots.add(new ItemOverlayState(stack, used.isPresent()));

            if (used.isPresent()) {
                ItemStack is = used.get();
                is.stackSize -= 1;
            }
        }

        return itemPresenceSlots;
    }

    private List<ItemStack> getFromInventory(List<Slot> inventorySlots, EntityClientPlayerMP thePlayer) {
        return inventorySlots.stream()
            .filter(
                s -> s != null && s.getStack() != null
                    && s.getStack().stackSize > 0
                    && s.isItemValid(s.getStack())
                    && s.canTakeStack(thePlayer))
            .map(
                s -> s.getStack()
                    .copy())
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
