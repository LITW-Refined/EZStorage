package com.zerofall.ezstorage.integration.waila;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZStorageUtils;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.plugins.PluginBase;

@Plugin(name = "EZStorage", deps = Reference.MOD_ID)
public class WailaPlugins extends PluginBase {

    private static DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    public void load(IWailaRegistrar registerar) {
        super.load(registerar);
        registerBody(BlockStorageCore.class);
    }

    @Override
    public void getBody(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor) {
        if (accessor.getTileEntity() instanceof TileEntityStorageCore core) {
            long itemsTotal = core.inventoryItemsStored;
            long itemsMax = core.inventoryItemsMax;
            int typesMax = core.inventoryTypesMax;
            tooltip.add(
                StatCollector.translateToLocalFormatted(
                    "hud.msg.ezstorage.core.itemscount",
                    formatter.format(itemsTotal),
                    formatter.format(itemsMax)));
            if (typesMax != 0 && typesMax < itemsMax) {
                int typesTotal = core.inventoryTypesStored;
                tooltip.add(
                    StatCollector.translateToLocalFormatted(
                        "hud.msg.ezstorage.core.typescount",
                        formatter.format(typesTotal),
                        formatter.format(typesMax)));
            }
            if (EZStorageUtils.isShiftDown()) {
                tooltip.add(
                    StatCollector.translateToLocalFormatted("hud.msg.ezstorage.core.inventoryid", core.inventoryId));
            }
        }
    }
}
