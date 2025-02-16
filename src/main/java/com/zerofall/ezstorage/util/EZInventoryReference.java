package com.zerofall.ezstorage.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class EZInventoryReference {

    public String inventoryId;
    public int blockDimId;
    public int blockX;
    public int blockY;
    public int blockZ;

    public EZInventoryReference() {}

    public EZInventoryReference(String inventoryId, int blockDimId, int blockX, int blockY, int blockZ) {
        this.inventoryId = inventoryId;
        this.blockDimId = blockDimId;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    public WorldServer getWorld() {
        MinecraftServer server = MinecraftServer.getServer();

        if (server == null) {
            return null;
        }

        for (WorldServer world : server.worldServers) {
            if (world.provider.dimensionId == blockDimId) {
                return world;
            }
        }

        return null;
    }
}
