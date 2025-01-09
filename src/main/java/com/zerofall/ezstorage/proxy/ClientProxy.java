package com.zerofall.ezstorage.proxy;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.integration.IntegrationUtils;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(EZStorage instance, FMLInitializationEvent event) {
        super.init(instance, event);
        IntegrationUtils.initClient();
    }
}
