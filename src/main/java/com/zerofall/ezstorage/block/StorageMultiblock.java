package com.zerofall.ezstorage.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZStorageUtils;

public class StorageMultiblock extends EZBlock {

    protected StorageMultiblock(String name, Material material) {
        super(name, material);
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, int x, int y, int z, int meta) {
        super.onBlockDestroyedByPlayer(worldIn, x, y, z, meta);
        attemptMultiblock(worldIn, x, y, z, null);
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, int x, int y, int z, Explosion explosionIn) {
        super.onBlockDestroyedByExplosion(worldIn, x, y, z, explosionIn);
        attemptMultiblock(worldIn, x, y, z, null);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        super.onBlockPlacedBy(worldIn, x, y, z, entity, itemStack);
        attemptMultiblock(worldIn, x, y, z, entity);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        if (!worldIn.isRemote) {
            Set<TileEntityStorageCore> coreSet = new HashSet<TileEntityStorageCore>();
            BlockRef br = new BlockRef(this, x, y, z);
            findMultipleCores(br, worldIn, null, coreSet);
            if (coreSet.size() > 1) {
                return false;
            }
        }
        return super.canPlaceBlockAt(worldIn, x, y, z);
    }

    /**
     * Attempt to form the multiblock structure by searching for the core, then telling the core to scan the multiblock
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public void attemptMultiblock(World world, int x, int y, int z, EntityLivingBase entity) {
        if (!world.isRemote) {
            if (!(this instanceof BlockStorageCore)) {
                BlockRef br = new BlockRef(this, x, y, z);
                TileEntityStorageCore core = findCore(br, world, null);
                if (core != null) {
                    core.scanMultiblock(entity);
                }
            }
        }
    }

    /**
     * Recursive function that searches for a StorageCore in a multiblock structure
     * 
     * @param br
     * @param world
     * @param scanned
     * @return
     */
    public TileEntityStorageCore findCore(BlockRef br, World world, Set<BlockRef> scanned) {
        if (scanned == null) {
            scanned = new HashSet<BlockRef>();
        }
        List<BlockRef> neighbors = EZStorageUtils.getNeighbors(br.posX, br.posY, br.posZ, world);
        for (BlockRef blockRef : neighbors) {
            if (blockRef.block instanceof StorageMultiblock) {
                if (blockRef.block instanceof BlockStorageCore) {
                    TileEntity te = world.getTileEntity(blockRef.posX, blockRef.posY, blockRef.posZ);
                    if (te instanceof TileEntityStorageCore) {
                        return (TileEntityStorageCore) te;
                    }
                } else {
                    if (scanned.add(blockRef) == true) {
                        TileEntityStorageCore entity = findCore(blockRef, world, scanned);
                        if (entity != null) {
                            return entity;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Recursive function that searches for a StorageCore in a multiblock structure
     * 
     * @param br
     * @param world
     * @param scanned
     * @return
     */
    public void findMultipleCores(BlockRef br, World world, Set<BlockRef> scanned, Set<TileEntityStorageCore> cores) {
        if (scanned == null) {
            scanned = new HashSet<BlockRef>();
        }
        List<BlockRef> neighbors = EZStorageUtils.getNeighbors(br.posX, br.posY, br.posZ, world);
        for (BlockRef blockRef : neighbors) {
            if (blockRef.block instanceof StorageMultiblock) {
                if (blockRef.block instanceof BlockStorageCore) {
                    TileEntity te = world.getTileEntity(blockRef.posX, blockRef.posY, blockRef.posZ);
                    if (te instanceof TileEntityStorageCore) {
                        cores.add((TileEntityStorageCore) te);
                    }
                } else {
                    if (scanned.add(blockRef) == true) {
                        findMultipleCores(blockRef, world, scanned, cores);
                    }
                }
            }
        }
    }
}
