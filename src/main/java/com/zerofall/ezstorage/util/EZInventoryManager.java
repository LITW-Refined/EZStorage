package com.zerofall.ezstorage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.network.server.MsgStorage;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

public class EZInventoryManager {

    private static final HashSet<EZInventory> inventories = new HashSet<>();

    public static EZInventory createInventory() {
        return createInventory(new EZInventory());
    }

    public static EZInventory createInventory(EZInventory inventory) {
        if (!inventories.contains(inventory)) {
            inventory.id = UUID.randomUUID()
                .toString();
            inventories.add(inventory);
        }
        inventory.resetHasChanges();
        return inventory;
    }

    public static EZInventory getInventory(String id) {
        // Find loaded inventory
        for (EZInventory inventory : inventories) {
            if (inventory.id.equals(id)) {
                return inventory;
            }
        }

        // Load inventory
        NBTTagCompound tag = readFromFile(getFilePath(id));
        if (tag != null) {
            EZInventory inventory = new EZInventory();
            if (inventory != null) {
                inventory.readFromNBT(tag);
                inventory.resetHasChanges();
                inventory.id = id;
                inventories.add(inventory);
                return inventory;
            }
        }

        // Inventory not found
        return null;
    }

    public static void saveInventories() {
        HashMap<String, NBTTagCompound> cache = new HashMap<String, NBTTagCompound>();

        // Write to NBT
        for (EZInventory inventory : inventories) {
            NBTTagCompound tag = new NBTTagCompound();
            inventory.writeToNBT(tag);
            inventory.resetHasChanges();
            cache.put(inventory.id, tag);
        }

        // Write to file
        if (!cache.isEmpty()) {
            new Thread(() -> {
                synchronized (inventories) {
                    for (Entry<String, NBTTagCompound> kvp : cache.entrySet()) {
                        File file = getFilePath(kvp.getKey());
                        saveToFile(kvp.getValue(), file);
                    }
                }
            }).start();
        }
    }

    public static void saveInventory(EZInventory inventory) {
        if (inventories.contains(inventory) && inventory.getHasChanges()) {
            NBTTagCompound tag = new NBTTagCompound();
            inventory.writeToNBT(tag);
            inventory.resetHasChanges();
            File file = getFilePath(inventory.id);
            new Thread(() -> {
                synchronized (inventories) {
                    saveToFile(tag, file);
                }
            }).start();
        }
    }

    public static void deleteInventory(EZInventory inventory) {
        if (inventories.remove(inventory)) {
            File file = getFilePath(inventory.id);
            new Thread(() -> {
                synchronized (inventories) {
                    file.delete();
                }
            }).start();;
        }
    }

    private static File getFilePath(String id) {
        // World root directory
        File worldDir = DimensionManager.getCurrentSaveRootDirectory();

        // EZInventory root directory
        File inventoryDir = new File(worldDir, Reference.MOD_ID + "/inventories");
        inventoryDir.mkdirs();

        // Inventory file
        return new File(inventoryDir, id.toString() + ".dat");
    }

    private static void saveToFile(NBTTagCompound tag, File file) {
        File fileNew = new File(file + ".new");
        File fileOld = new File(file + ".old");

        try {
            // Write to new temporary file
            FileOutputStream outputStream = new FileOutputStream(fileNew);
            CompressedStreamTools.writeCompressed(tag, outputStream);
            outputStream.close();

            // Delete old backup file
            if (fileOld.exists()) {
                fileOld.delete();
            }

            // Rename existing file to old backup file
            file.renameTo(fileOld);

            // Delete current existing file
            if (file.exists()) {
                file.delete();
            }

            // Rename new temporary file
            if (!fileNew.renameTo(file)) {
                throw new IOException("Couldn't rename new temporary file.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            EZStorage.instance.LOG.warn("Couldn't write inventory to file system.", ex);
        }
    }

    private static NBTTagCompound readFromFile(File file) {
        File fileOld = new File(file + ".old");
        NBTTagCompound tag;

        try {
            FileInputStream inputStream = new FileInputStream(file);
            tag = CompressedStreamTools.readCompressed(inputStream);
            inputStream.close();
        } catch (IOException ex) {
            EZStorage.instance.LOG.warn("Couldn't read inventory file. Try falling back to backup, if exists.", ex);
            ex.printStackTrace();
            tag = null;
        }

        if (tag == null && fileOld.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(fileOld);
                tag = CompressedStreamTools.readCompressed(inputStream);
                inputStream.close();
            } catch (IOException ex) {
                EZStorage.instance.LOG.warn("Couldn't read inventory backup file.", ex);
                ex.printStackTrace();
            }
        }

        return tag;
    }

    public static void sendToClients(EZInventory inventory) {
        sendToClients(inventory, true);
    }

    public static void sendToClients(EZInventory inventory, boolean checkTileEntities) {
        if (inventory == null || !inventories.contains(inventory)) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }

        for (WorldServer world : server.worldServers) {
            // Send inventory packet to players with open Storage Core gui
            for (EntityPlayer player : world.playerEntities) {
                if (player.openContainer instanceof ContainerStorageCore && player instanceof EntityPlayerMP playerMP) {
                    EZStorage.instance.network.sendTo(new MsgStorage(inventory), playerMP);
                }
            }

            // Update Storage Core tile entities
            if (checkTileEntities) {
                for (TileEntity tileEntity : world.loadedTileEntityList) {
                    if (tileEntity instanceof TileEntityStorageCore core && core.getInventory() == inventory) {
                        core.updateTileEntity(false);
                    }
                }
            }
        }
    }
}
