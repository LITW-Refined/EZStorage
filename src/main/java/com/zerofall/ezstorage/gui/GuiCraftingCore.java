package com.zerofall.ezstorage.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;

public class GuiCraftingCore extends GuiStorageCore {

    public GuiCraftingCore(EntityPlayer player, World world, int x, int y, int z) {
        super(new ContainerStorageCoreCrafting(player, world, x, y, z), world, x, y, z);
        this.xSize = 195;
        this.ySize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();
        IntegrationUtils.applyCraftingTweaks(this, extraButtons, inventorySlots.inventorySlots.size() - 9);
    }

    @Override
    public int rowsVisible() {
        return 5;
    }

    @Override
    protected ResourceLocation getBackground() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/storageCraftingGui.png");
    }
}
