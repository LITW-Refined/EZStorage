package com.zerofall.ezstorage;

import com.zerofall.ezstorage.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]")
public class EZStorage {

    @Mod.Instance(Reference.MOD_ID)
    public static EZStorage instance;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    public SimpleNetworkWrapper networkWrapper;
    public EZTab creativeTab;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(this, event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(this, event);
    }
}
