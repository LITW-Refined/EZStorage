package com.zerofall.ezstorage.events;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.input.Keyboard;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.enums.OpenInvGuiSource;
import com.zerofall.ezstorage.integration.ModIds;
import com.zerofall.ezstorage.network.client.MsgPickBlockFromTerminal;
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
    public KeyBinding keybindBulkAction;

    @SideOnly(Side.CLIENT)
    public KeyBinding keybindDropItem;

    @SideOnly(Side.CLIENT)
    public KeyBinding keybindPickBlock;

    @SideOnly(Side.CLIENT)
    public void initKeybinds() {
        keybindOpenTerminal = new KeyBinding(
            "key.ezstorage.open_terminal",
            Keyboard.CHAR_NONE,
            "key.categories.ezstorage");
        ClientRegistry.registerKeyBinding(keybindOpenTerminal);

        keybindBulkAction = new KeyBinding("key.ezstorage.bulk_action", Keyboard.KEY_SPACE, "key.categories.ezstorage");
        ClientRegistry.registerKeyBinding(keybindBulkAction);

        keybindDropItem = new KeyBinding("key.ezstorage.drop_item", Keyboard.KEY_Q, "key.categories.ezstorage");
        ClientRegistry.registerKeyBinding(keybindDropItem);

        keybindPickBlock = new KeyBinding("key.ezstorage.pick_block", Keyboard.CHAR_NONE, "key.categories.ezstorage");
        ClientRegistry.registerKeyBinding(keybindPickBlock);
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
        if (p != null && keybindPickBlock.isPressed()) {
            handlePickBlock();
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean handlePickBlock() {
        Minecraft mc = Minecraft.getMinecraft();

        // Must be in-game (no GUI open)
        if (mc.currentScreen != null) {
            return false;
        }

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) {
            return false;
        }

        // Check if Baubles is loaded
        if (!ModIds.BAUBLES.isLoaded()) {
            return false;
        }

        // Get the block the player is looking at
        MovingObjectPosition mouseOver = mc.objectMouseOver;
        if (mouseOver == null || mouseOver.typeOfHit != MovingObjectType.BLOCK) {
            return false;
        }

        int x = mouseOver.blockX;
        int y = mouseOver.blockY;
        int z = mouseOver.blockZ;
        Block block = player.worldObj.getBlock(x, y, z);

        if (block == null || block.isAir(player.worldObj, x, y, z)) {
            return false;
        }

        // Get the item representation of the looked-at block
        ItemStack pickStack = block.getPickBlock(mouseOver, player.worldObj, x, y, z);
        if (pickStack == null) {
            return false;
        }

        // Use a copy to avoid NBT pollution
        ItemStack targetStack = pickStack.copy();
        targetStack.stackSize = 1;

        // Send to server
        EZStorage.instance.network.sendToServer(new MsgPickBlockFromTerminal(targetStack));
        return true;
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
