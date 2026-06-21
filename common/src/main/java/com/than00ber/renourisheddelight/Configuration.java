package com.than00ber.renourisheddelight;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = RenourishedDelightMod.MOD_ID)
public class Configuration implements ConfigData {

    public static void init() {
        AutoConfig.register(Configuration.class, GsonConfigSerializer::new);
    }

    public static Configuration getInstance() {
        return AutoConfig.getConfigHolder(Configuration.class).getConfig();
    }

    public int foodBarOffsetX = 0;
    public int foodBarOffsetY = 0;
}