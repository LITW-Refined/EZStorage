package com.zerofall.ezstorage.mixins.late;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.zerofall.ezstorage.gui.GuiStorageCore;

import codechicken.nei.ItemStackAmount;
import codechicken.nei.recipe.AutoCraftingManager;

@Mixin(AutoCraftingManager.class)
public abstract class Mixin_NEI_AutoCraftInventory {

    @Inject(method = "getInventoryItems", at = @At("RETURN"), remap = false)
    private static void ezstorage$includeStorageItems(GuiContainer guiContainer,
        CallbackInfoReturnable<ItemStackAmount> cir) {
        if (!(guiContainer instanceof GuiStorageCore storageGui)) {
            return;
        }
        cir.getReturnValue()
            .addAll(storageGui.copyStorageStacks());
    }
}
