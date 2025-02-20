package com.zerofall.ezstorage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zerofall.ezstorage.gui.GuiHandler;
import com.zerofall.ezstorage.network.client.HandlerMsgStorage;
import com.zerofall.ezstorage.network.client.MsgClearCraftingGrid;
import com.zerofall.ezstorage.network.client.MsgInvSlotClicked;
import com.zerofall.ezstorage.network.client.MsgReqCrafting;
import com.zerofall.ezstorage.network.client.MsgReqOpenInvGui;
import com.zerofall.ezstorage.network.client.MsgReqStorage;
import com.zerofall.ezstorage.network.server.HandlerMsgClearCraftingGrid;
import com.zerofall.ezstorage.network.server.HandlerMsgInvSlotClicked;
import com.zerofall.ezstorage.network.server.HandlerMsgReqCrafting;
import com.zerofall.ezstorage.network.server.HandlerMsgReqOpenInvGui;
import com.zerofall.ezstorage.network.server.HandlerMsgReqStorage;
import com.zerofall.ezstorage.network.server.MsgStorage;
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
    public final EZTab creativeTab = new EZTab();
    public final Logger LOG = LogManager.getLogger(Reference.MOD_ID);
    public final GuiHandler guiHandler = new GuiHandler();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(this, event);

        // Register gui handler
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, guiHandler);

        // Register network handler & packets
        instance.network = NetworkRegistry.INSTANCE.newSimpleChannel("ezChannel");
        Integer d = 0;
        instance.network.registerMessage(HandlerMsgInvSlotClicked.class, MsgInvSlotClicked.class, d++, Side.SERVER);
        instance.network.registerMessage(HandlerMsgReqCrafting.class, MsgReqCrafting.class, d++, Side.SERVER);
        instance.network.registerMessage(HandlerMsgReqOpenInvGui.class, MsgReqOpenInvGui.class, d++, Side.SERVER);
        instance.network.registerMessage(HandlerMsgReqStorage.class, MsgReqStorage.class, d++, Side.SERVER);
        instance.network.registerMessage(HandlerMsgStorage.class, MsgStorage.class, d++, Side.CLIENT);
        instance.network
            .registerMessage(HandlerMsgClearCraftingGrid.class, MsgClearCraftingGrid.class, d++, Side.SERVER);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(this, event);
    }
}
