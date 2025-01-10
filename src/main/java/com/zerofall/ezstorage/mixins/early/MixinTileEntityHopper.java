package com.zerofall.ezstorage.mixins.early;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;

@Mixin(TileEntityHopper.class)
public abstract class MixinTileEntityHopper {

    @WrapOperation(
        method = "func_145899_c",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/IInventory;getStackInSlot(I)Lnet/minecraft/item/ItemStack;"),
        remap = false)
    private static ItemStack ezstorage$TileEntityHopper$fixInputToTileEntityInputPort(IInventory inventory, int slot,
        Operation<ItemStack> original) {
        if (inventory instanceof TileEntityInventoryProxy) {
            // Force calling setInventorySlotContents() as we can't support directly editing stack size of ItemStack (it
            // can only be handled handled via ItemGroup.count)!
            return null;
        }
        return original.call(inventory, slot);
    }
}
