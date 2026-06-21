package com.than00ber.renourisheddelight;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

public final class Configuration {

    public static void init() {
        AutoConfig.register(Client.class, JanksonConfigSerializer::new);
    }

    @Config(name = RenourishedDelightMod.MOD_ID + "/client")
    public static final class Client implements ConfigData {

        public static Client getInstance() {
            return AutoConfig.getConfigHolder(Client.class).getConfig();
        }

        @Comment("Horizontal pixel offset for the food display UI (default: 0)")
        public int foodBarOffsetX = 0;
        @Comment("Vertical pixel offset for the food display UI (default: 0)")
        public int foodBarOffsetY = 0;
    }
}