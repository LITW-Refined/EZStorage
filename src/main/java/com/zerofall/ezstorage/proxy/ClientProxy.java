package com.zerofall.ezstorage.proxy;

import com.zerofall.ezstorage.init.EZBlocks;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenders() {
        EZBlocks.registerRenders();
    }
}
