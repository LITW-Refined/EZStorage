package com.zerofall.ezstorage.block;

import net.minecraft.block.material.Material;

import com.zerofall.ezstorage.configuration.EZConfiguration;

public class BlockHyperStorage extends BlockStorage {

    public BlockHyperStorage() {
        super("hyper_storage_box", Material.iron);
    }

    @Override
    public int getCapacity() {
        return EZConfiguration.hyperCapacity;
    }
}
