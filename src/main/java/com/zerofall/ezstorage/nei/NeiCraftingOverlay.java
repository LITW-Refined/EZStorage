package com.zerofall.ezstorage.nei;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.network.RecipeMessage;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class NeiCraftingOverlay implements IOverlayHandler {

    @Override
    public void overlayRecipe(final GuiContainer gui, final IRecipeHandler recipe, final int recipeIndex,
        final boolean shift) {
        try {
            final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
            overlayRecipe(gui, ingredients, shift);
        } catch (final Exception localException) {} catch (final Error localError) {}
    }

    public void overlayRecipe(final GuiContainer gui, final List<PositionedStack> ingredients, final boolean shift) {
        try {
            final NBTTagCompound recipe = new NBTTagCompound();
            if (gui instanceof com.zerofall.ezstorage.gui.GuiCraftingCore) {
                for (final Iterator<PositionedStack> localIterator1 = ingredients.iterator(); localIterator1
                    .hasNext();) {
                    final PositionedStack positionedStack = localIterator1.next();
                    final int col = (positionedStack.relx - 25) / 18;
                    final int row = (positionedStack.rely - 6) / 18;
                    if (positionedStack.items != null && positionedStack.items.length > 0)
                        for (final Object obj : gui.inventorySlots.inventorySlots) {
                            final Slot slot = (Slot) obj;
                            if (slot.inventory instanceof InventoryCrafting && slot.getSlotIndex() == col + row * 3) {
                                final NBTTagList tags = new NBTTagList();
                                final List<ItemStack> list = new LinkedList<ItemStack>();
                                for (int x = 0; x < positionedStack.items.length; x++)
                                    list.add(positionedStack.items[x]);
                                for (final ItemStack is : list) {
                                    final NBTTagCompound tag = new NBTTagCompound();
                                    is.writeToNBT(tag);
                                    tags.appendTag(tag);
                                }
                                recipe.setTag("#" + slot.getSlotIndex(), tags);
                                break;
                            }
                        }
                }
                EZStorage.networkWrapper.sendToServer(new RecipeMessage(recipe));
            }
        } catch (final Exception localException) {} catch (final Error localError) {}
    }
}
