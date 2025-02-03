package com.zerofall.ezstorage;

import com.zerofall.ezstorage.gui.GuiHandler;
import com.zerofall.ezstorage.network.InvSlotClickedMsg;
import com.zerofall.ezstorage.network.InvSlotClickedMsgHandler;
import com.zerofall.ezstorage.network.ReqCraftingMsg;
import com.zerofall.ezstorage.network.ReqCraftingMsgHander;
import com.zerofall.ezstorage.network.ReqOpenInvGuiMsg;
import com.zerofall.ezstorage.network.ReqOpenInvGuiMsgHandler;
import com.zerofall.ezstorage.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

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

    public SimpleNetworkWrapper network;
    public EZTab creativeTab = new EZTab();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(this, event);

        // Register gui handler
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        // Register network handler & packets
        instance.network = NetworkRegistry.INSTANCE.newSimpleChannel("ezChannel");
        Integer d = 0;
        instance.network.registerMessage(InvSlotClickedMsgHandler.class, InvSlotClickedMsg.class, d++, Side.SERVER);
        instance.network.registerMessage(ReqCraftingMsgHander.class, ReqCraftingMsg.class, d++, Side.SERVER);
        instance.network.registerMessage(ReqOpenInvGuiMsgHandler.class, ReqOpenInvGuiMsg.class, d++, Side.SERVER);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(this, event);
    }
}
