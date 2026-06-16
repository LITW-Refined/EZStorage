package com.zerofall.ezstorage.mixins.late;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.zerofall.ezstorage.gui.GuiStorageCore;
import com.zerofall.ezstorage.util.EZInventory;

import codechicken.nei.ItemStackAmount;
import codechicken.nei.recipe.AutoCraftingManager;

@Mixin(AutoCraftingManager.class)
public abstract class MixinNEIAutoCraftingManager {

    @Inject(method = "getInventoryItems", at = @At("RETURN"), cancellable = true, remap = false)
    private static void ezstorage$injectStorageItems(GuiContainer guiContainer,
        CallbackInfoReturnable<ItemStackAmount> cir) {
        if (guiContainer instanceof GuiStorageCore) {
            ItemStackAmount items = cir.getReturnValue();
            if (items == null) return;
            EZInventory inv = ((GuiStorageCore) guiContainer).getInventory();
            if (inv != null) {
                for (ItemStack stack : new ArrayList<>(inv.inventory)) {
                    if (stack != null && stack.stackSize > 0) {
                        items.add(stack);
                    }
                }
            }
            cir.setReturnValue(items);
        }
    }
}
