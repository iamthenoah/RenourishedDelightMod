package com.than00ber.renourisheddelight;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = RenourishedDelightMod.MOD_ID)
public class Configuration implements ConfigData {

    @Comment("Horizontal pixel offset for the food display UI (default: 0)")
    public int foodBarOffsetX = 0;
    @Comment("Vertical pixel offset for the food display UI (default: 0)")
    public int foodBarOffsetY = 0;

    public static Configuration getInstance() {
        return AutoConfig.getConfigHolder(Configuration.class).getConfig();
    }

    public static void init() {
        AutoConfig.register(Configuration.class, JanksonConfigSerializer::new);
    }
}