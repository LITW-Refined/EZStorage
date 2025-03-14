package com.zerofall.ezstorage.configuration;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.zerofall.ezstorage.Reference;

@Config(modid = Reference.MOD_ID)
public class EZConfiguration {

    @Config.Comment("Count of items that the basic storage box can hold.")
    @Config.DefaultInt(400)
    @Config.RangeInt(min = 1)
    public static int basicCapacity;

    @Config.Comment("Count of items that the condensed storage box can hold.")
    @Config.DefaultInt(4000)
    @Config.RangeInt(min = 1)
    public static int condensedCapacity;

    @Config.Comment("Count of items that the hyper storage box can hold.")
    @Config.DefaultInt(400000)
    @Config.RangeInt(min = 1)
    public static int hyperCapacity;

    @Config.Comment("The maximum amount of different items that can be stored within one storage core.\nIn the past this option tried to ensure the NBT data wont get too large wich would normally lead to world corruption. Today it is not needed anymore, that's why the default value is 0 (disbaled).")
    @Config.DefaultInt(0)
    @Config.RangeInt(min = 0)
    public static int maxItemTypes;

    @Config.Comment("Focus the input field when opening a storage GUI.")
    @Config.DefaultBoolean(true)
    public static boolean focusGuiInput;

    public static void init() {
        try {
            ConfigurationManager.registerConfig(EZConfiguration.class);
        } catch (ConfigException ignore) {}
    }
}
