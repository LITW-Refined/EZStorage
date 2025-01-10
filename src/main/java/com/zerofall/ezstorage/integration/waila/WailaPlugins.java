package com.zerofall.ezstorage.integration.waila;

import java.text.DecimalFormat;
import java.util.List;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
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
            long itemsTotal = core.inventory.getTotalCount();
            long itemsMax = core.inventory.maxItems;
            int typesTotal = core.inventory.slotCount();
            int typesMax = EZConfiguration.maxItemTypes;
            tooltip.add(String.format(StatCollector.translateToLocal("hud.msg.core.itemscount"), formatter.format(itemsTotal), formatter.format(itemsMax)));
            tooltip.add(String.format(StatCollector.translateToLocal("hud.msg.core.typescount"), formatter.format(typesTotal), formatter.format(typesMax)));
        }
    }
}

