package com.zerofall.ezstorage.mixins.late;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

import mcp.mobius.betterbarrels.common.items.dolly.ItemBarrelMover;

@Mixin(ItemBarrelMover.class)
public abstract class Mixin_Jabba_ItemBarrelMover {

    @ModifyReturnValue(
        method = "isTEMovable",
        at = @At("TAIL"), // Use TAIL instead of RETURN to only modify the last return and not each return.
        remap = false)
    private boolean ezstorage$isTEMovable$allowStorageBox(boolean original, TileEntity container) {
        return original || container instanceof TileEntityStorageCore;
    }
}
