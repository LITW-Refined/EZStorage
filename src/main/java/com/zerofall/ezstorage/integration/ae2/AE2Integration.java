package com.zerofall.ezstorage.integration.ae2;

import appeng.api.AEApi;
import appeng.api.storage.IExternalStorageHandler;

public class AE2Integration {

    public static void register() {
        IExternalStorageHandler handler = (IExternalStorageHandler) new AE2StorageHandler();
        AEApi.instance()
            .registries()
            .externalStorage()
            .addExternalStorageInterface(handler);
    }
}
