package com.zerofall.ezstorage.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.EZTab;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.events.XEventHandler;
import com.zerofall.ezstorage.gui.GuiHandler;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.network.MyMessage;
import com.zerofall.ezstorage.network.PacketHandler;
import com.zerofall.ezstorage.network.RecipeMessage;
import com.zerofall.ezstorage.network.RecipePacketHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {

    public void preInit(EZStorage instance, FMLPreInitializationEvent event) {
        instance.config = new Configuration(event.getSuggestedConfigurationFile());
        EZConfiguration.syncConfig();
        instance.creativeTab = new EZTab();
        EZBlocks.init();
        EZBlocks.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        instance.networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("ezChannel");
        instance.networkWrapper.registerMessage(PacketHandler.class, MyMessage.class, 0, Side.SERVER);
        instance.networkWrapper.registerMessage(RecipePacketHandler.class, RecipeMessage.class, 1, Side.SERVER);
        MinecraftForge.EVENT_BUS.register(new XEventHandler());
    }

    public void init(EZStorage instance, FMLInitializationEvent event) {
        EZBlocks.registerRecipes();
    }
}
