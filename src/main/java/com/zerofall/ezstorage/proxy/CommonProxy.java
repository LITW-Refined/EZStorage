package com.zerofall.ezstorage.proxy;

import net.minecraftforge.common.MinecraftForge;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.events.EZEventHandler;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.init.EZItems;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.integration.ModIds;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public final EZEventHandler eventHandler = new EZEventHandler();

    public void preInit(EZStorage instance, FMLPreInitializationEvent event) {
        // Initialize configuration
        EZConfiguration.init();

        // Register blocks
        EZBlocks.init();
        EZBlocks.register();

        // Register items
        EZItems.init();
        EZItems.register();

        // Register events
        MinecraftForge.EVENT_BUS.register(eventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(eventHandler);
    }

    public void init(EZStorage instance, FMLInitializationEvent event) {
        // Register recipes
        EZBlocks.registerRecipes();
        EZItems.registerRecipes();

        // Register integrations
        IntegrationUtils.init();

        // Set maxItemTypes
        if (EZConfiguration.maxItemTypesAutoMode) {
            EZConfiguration.maxItemTypes = ModIds.HODGEPODGE.isLoaded() ? 65000 : 1000;
            EZConfiguration.save();
            EZStorage.instance.LOG.info("Automatically set maxItemTypes to " + EZConfiguration.maxItemTypes);
        }
    }
}
