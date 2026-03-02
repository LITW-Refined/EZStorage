package com.zerofall.ezstorage.integration;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import com.zerofall.ezstorage.gui.GuiStorageCore;

import codechicken.nei.api.INEIGuiAdapter;
import codechicken.nei.recipe.StackInfo;

// maybe i can put it in a NEI folder but well
// maybe i can can implement it differently but im tired
public class EZNEIHandler extends INEIGuiAdapter {

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        if (!(gui instanceof GuiStorageCore)) {
            return false;
        }
        GuiStorageCore ezGui = (GuiStorageCore) gui;
        if (draggedStack == null || draggedStack.getItem() == null) {
            return false;
        }
        if (!ezGui.isOverTextField(mousex, mousey)) {
            return false;
        }
        FluidStack fluidStack = StackInfo.isFluidContainer(draggedStack) ? null : StackInfo.getFluid(draggedStack);
        String displayName;
        if (fluidStack != null) {
            displayName = fluidStack.getLocalizedName();
        } else {
            displayName = draggedStack.getDisplayName();
        }
        displayName = EnumChatFormatting.getTextWithoutFormattingCodes(displayName);
        ezGui.setTextFieldValue(displayName, mousex, mousey, draggedStack.copy());
        return true;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return false;
    }

    @Override
    public List getInventoryAreas(GuiContainer gui) {
        return Collections.emptyList();
    }
}
