package com.zerofall.ezstorage.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgClearCraftingGrid;

import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiCraftingCore extends GuiStorageCore {

    protected final GuiButtonExt btnClearCraftingPanel = new GuiButtonExt(10, 215, 106, 8, 8, "");

    public GuiCraftingCore(EntityPlayer player, World world, int x, int y, int z) {
        super(new ContainerStorageCoreCrafting(player, world), world, x, y, z);
        this.xSize = 195;
        this.ySize = 256;
        IntegrationUtils.applyCraftingTweaks(this);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(btnClearCraftingPanel);
    }

    @Override
    public int rowsVisible() {
        return 5;
    }

    @Override
    protected ResourceLocation getBackground() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/storageCraftingGui.png");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == btnClearCraftingPanel) {
            EZStorage.instance.network.sendToServer(new MsgClearCraftingGrid());
        }
    }
}
