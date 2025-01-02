package com.zerofall.ezstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;

public class EZBlock extends Block {

    protected EZBlock(String name, Material materialIn) {
        super(materialIn);
        this.setBlockName(name);
        this.setBlockTextureName(Reference.MOD_ID + ":" + name);
        this.setCreativeTab(EZStorage.instance.creativeTab);
        this.setHardness(2.0f);
    }

}
