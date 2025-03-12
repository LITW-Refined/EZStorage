package com.zerofall.ezstorage.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EZStorageUtils {

    public static List<BlockRef> getNeighbors(int xCoord, int yCoord, int zCoord, World world) {
        List<BlockRef> blockList = new ArrayList<BlockRef>();
        blockList.add(new BlockRef(world.getBlock(xCoord - 1, yCoord, zCoord), xCoord - 1, yCoord, zCoord));
        blockList.add(new BlockRef(world.getBlock(xCoord + 1, yCoord, zCoord), xCoord + 1, yCoord, zCoord));
        blockList.add(new BlockRef(world.getBlock(xCoord, yCoord - 1, zCoord), xCoord, yCoord - 1, zCoord));
        blockList.add(new BlockRef(world.getBlock(xCoord, yCoord + 1, zCoord), xCoord, yCoord + 1, zCoord));
        blockList.add(new BlockRef(world.getBlock(xCoord, yCoord, zCoord - 1), xCoord, yCoord, zCoord - 1));
        blockList.add(new BlockRef(world.getBlock(xCoord - 1, yCoord, zCoord), xCoord - 1, yCoord, zCoord));
        blockList.add(new BlockRef(world.getBlock(xCoord, yCoord, zCoord + 1), xCoord, yCoord, zCoord + 1));
        return blockList;
    }

    @SideOnly(Side.CLIENT)
    public static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @SideOnly(Side.CLIENT)
    public static boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }
}
