package com.zerofall.ezstorage.block;

import net.minecraft.block.material.Material;

import com.zerofall.ezstorage.configuration.EZConfiguration;

public class BlockCondensedStorage extends BlockStorage {

    public BlockCondensedStorage() {
        super("condensed_storage_box", Material.iron);
    }

    @Override
    public int getCapacity() {
        return EZConfiguration.condensedCapacity;
    }
}
