package com.zerofall.ezstorage.recipes;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import com.zerofall.ezstorage.enums.PortableStoragePanelTier;
import com.zerofall.ezstorage.item.ItemPortableStoragePanel;

public class PortableStoragePanelUpgradeRecipe implements IRecipe {

    private ItemStack result;

    @Override
    public boolean matches(InventoryCrafting craftingGridInventory, World world) {
        result = null;
        ItemStack panelStack = null;
        ItemPortableStoragePanel panelItem = null;
        boolean hasRedstone = false;
        boolean hasUpgradeItem = false;
        PortableStoragePanelTier tier = null;
        PortableStoragePanelTier nextTier = null;
        Item upgradeItem = null;

        for (int i = 0; i < craftingGridInventory.getSizeInventory(); i++) {
            ItemStack slotStack = craftingGridInventory.getStackInSlot(i);
            if (slotStack == null) {
                continue;
            }

            Item slotItem = slotStack.getItem();
            if (panelStack == null && slotItem instanceof ItemPortableStoragePanel panel) {
                panelStack = slotStack;
                panelItem = panel;
                tier = panelItem.getTier(panelStack);
                nextTier = PortableStoragePanelTier.getNextTier(tier);
                upgradeItem = getUpgradeItem(nextTier);
            } else if (!hasRedstone && slotItem == Items.redstone) {
                hasRedstone = true;
            } else if (!hasUpgradeItem && slotItem == upgradeItem) {
                hasUpgradeItem = true;
            } else {
                break;
            }
        }

        if (hasRedstone && hasUpgradeItem
            && panelStack != null
            && panelItem != null
            && tier != null
            && nextTier != null) {
            result = panelStack.copy();
            panelItem.setTier(result, nextTier);
            return true;
        }

        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting p_77572_1_) {
        if (result != null) {
            return result.copy();
        }
        return null;
    }

    @Override
    public int getRecipeSize() {
        return 3;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return result;
    }

    private static Item getUpgradeItem(PortableStoragePanelTier tier) {
        switch (tier) {
            case TIER_2:
                return Items.ender_pearl;
            case TIER_3:
                return Items.ender_eye;
            case TIER_INFINITY:
                return Items.nether_star;
            default:
                return Items.ender_eye;
        }
    }
}
