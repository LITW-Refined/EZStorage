package com.zerofall.ezstorage.events;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.input.Keyboard;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.enums.OpenInvGuiSource;
import com.zerofall.ezstorage.network.client.MsgReqOpenInvGui;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EZEventHandler {

    @SideOnly(Side.CLIENT)
    private KeyBinding keybindOpenTerminal;

    @SideOnly(Side.CLIENT)
    public void initKeybinds() {
        keybindOpenTerminal = new KeyBinding(
            "key.ezstorage.open_terminal",
            Keyboard.CHAR_NONE,
            "key.categories.ezstorage");
        ClientRegistry.registerKeyBinding(keybindOpenTerminal);
    }

    @SubscribeEvent
    public void onBlockBreak(BreakEvent e) {
        if (!e.world.isRemote) {
            TileEntity tileentity = e.world.getTileEntity(e.x, e.y, e.z);
            if (tileentity instanceof TileEntityStorageCore core) {
                EZInventory inventory = EZInventoryManager.getInventory(core.inventoryId);
                if (inventory != null && inventory.getTotalCount() > 0) {
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyInput(KeyInputEvent event) {
        FMLClientHandler fmlClientHandler = FMLClientHandler.instance();
        EntityClientPlayerMP p = fmlClientHandler.getClient().thePlayer;
        if (p != null && keybindOpenTerminal.isPressed()) {
            EZStorage.instance.network.sendToServer(new MsgReqOpenInvGui(OpenInvGuiSource.BAUBLES));
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            EZInventoryManager.saveInventories();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            EZInventoryManager.clearCache();
        }
    }
}
