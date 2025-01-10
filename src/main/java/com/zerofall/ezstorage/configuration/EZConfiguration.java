package com.zerofall.ezstorage.configuration;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.zerofall.ezstorage.Reference;

@Config(modid = Reference.MOD_ID)
public class EZConfiguration {

    @Config.Name("capacityStorageBasic")
    @Config.Comment("Count of items that the basic storage box can hold.")
    @Config.DefaultInt(400)
    @Config.RangeInt(min = 1)
    public static int basicCapacity;

    @Config.Name("capacityStorageCondensed")
    @Config.Comment("Count of items that the condensed storage box can hold.")
    @Config.DefaultInt(4000)
    @Config.RangeInt(min = 1)
    public static int condensedCapacity;

    @Config.Name("capacityStorageHyper")
    @Config.Comment("Count of items that the hyper storage box can hold.")
    @Config.DefaultInt(400000)
    @Config.RangeInt(min = 1)
    public static int hyperCapacity;

    public static void init() {
        try {
            ConfigurationManager.registerConfig(EZConfiguration.class);
        } catch (ConfigException ignore) {}
    }
}
