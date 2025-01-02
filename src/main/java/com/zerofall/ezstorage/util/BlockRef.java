package com.zerofall.ezstorage.util;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public class BlockRef {

    public int posX;
    public int posY;
    public int posZ;
    public Block block;

    public BlockRef(Block block, int x, int y, int z) {
        this.block = block;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public BlockRef(TileEntity entity) {
        this.block = entity.getBlockType();
        this.posX = entity.xCoord;
        this.posY = entity.yCoord;
        this.posZ = entity.zCoord;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((block == null) ? 0 : block.hashCode());
        result = prime * result + posX + posY + posZ;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BlockRef other = (BlockRef) obj;
        if (block == null) {
            if (other.block != null) return false;
        } else if (!Block.isEqualTo(block, other.block)) return false;
        if (posX != other.posX || posY != other.posY || posZ != other.posZ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BlockRef [pos=" + posX + ";" + posY + ";" + posZ + ", block=" + block + "]";
    }
}
