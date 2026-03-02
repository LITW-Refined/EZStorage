package com.zerofall.ezstorage.integration;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import cpw.mods.fml.common.Optional;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.IConfigureNEI")
public class EZNEIPlugin implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerNEIGuiHandler(new EZNEIHandler());
    }

    @Override
    public String getName() {
        return "EZStorage";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
