package com.zerofall.ezstorage.nei;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.gui.GuiStorageCore;
import com.zerofall.ezstorage.network.client.MsgReqCrafting;
import com.zerofall.ezstorage.util.EZInventory;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton.ItemOverlayState;
import codechicken.nei.recipe.IRecipeHandler;

public class NeiCraftingOverlay implements IOverlayHandler {

    @Override
    public void overlayRecipe(final GuiContainer gui, final IRecipeHandler recipe, final int recipeIndex,
        final boolean shift) {
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
        overlayRecipe(gui, ingredients, shift);
    }

    public void overlayRecipe(final GuiContainer gui, final List<PositionedStack> ingredients, final boolean shift) {
        if (!(gui instanceof com.zerofall.ezstorage.gui.GuiCraftingCore)) {
            return;
        }

        final NBTTagCompound recipe = new NBTTagCompound();

        for (final PositionedStack positionedStack : ingredients) {
            if (positionedStack == null || positionedStack.items == null || positionedStack.items.length == 0) {
                continue;
            }

            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;

            for (final Slot slot : gui.inventorySlots.inventorySlots) {
                if (!(slot.inventory instanceof InventoryCrafting) || slot.getSlotIndex() != col + row * 3) {
                    continue;
                }

                final NBTTagList tags = new NBTTagList();
                final List<ItemStack> list = new LinkedList<ItemStack>();

                for (int x = 0; x < positionedStack.items.length; x++) {
                    list.add(positionedStack.items[x]);
                }

                for (final ItemStack is : list) {
                    final NBTTagCompound tag = new NBTTagCompound();
                    is.writeToNBT(tag);
                    tags.appendTag(tag);
                }

                recipe.setTag("#" + slot.getSlotIndex(), tags);
                break;
            }
        }

        EZStorage.instance.network.sendToServer(new MsgReqCrafting(recipe));
    }

    @Override
    public List<ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        List<ItemStack> invStacks = new ArrayList<ItemStack>();

        // Collect slots of storage core
        if (firstGui instanceof GuiStorageCore coreGui) {
            EZInventory inventory = coreGui.getInventory();
            if (inventory != null) {
                invStacks.addAll(
                    inventory.inventory.stream()
                        .map(s -> s.copy())
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
        }

        // Collect slots of player inventory
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
