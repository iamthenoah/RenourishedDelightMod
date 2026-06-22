package com.than00ber.renourisheddelight;

import com.than00ber.renourisheddelight.food.ConsumableFood;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Configuration {

    public static void init() {
        AutoConfig.register(Client.class, JanksonConfigSerializer::new);
        AutoConfig.register(Common.class, JanksonConfigSerializer::new);
    }

    @Config(name = RenourishedDelightMod.MOD_ID + "/client")
    public static final class Client implements ConfigData {

        public static Client getInstance() {
            return AutoConfig.getConfigHolder(Client.class).getConfig();
        }

        @ConfigEntry.Gui.Tooltip
        @Comment("Horizontal pixel offset for the food display UI (default: 0)")
        public int foodBarOffsetX = 0;

        @ConfigEntry.Gui.Tooltip
        @Comment("Vertical pixel offset for the food display UI (default: 0)")
        public int foodBarOffsetY = 0;
    }

    @Config(name = RenourishedDelightMod.MOD_ID + "/common")
    public static final class Common implements ConfigData {

        public static Common getInstance() {
            return AutoConfig.getConfigHolder(Common.class).getConfig();
        }

        public FoodItemConfiguration getItemConfig(Item item) {
            if (item.components().has(DataComponents.FOOD)) {
                String id = BuiltInRegistries.ITEM.getKey(item).toString();
                Common common = Common.getInstance();
                FoodItemConfiguration existing = common.foodItemConfigurations.get(id);
    
                if (existing == null) {
                    FoodProperties properties = item.components().get(DataComponents.FOOD);
                    int nutrition = properties != null ? properties.nutrition() : 2;
                    float saturation = properties != null ? properties.saturation() : 0.0F;
    
                    existing = new FoodItemConfiguration();
                    existing.hearts = ConsumableFood.toHearts(nutrition, saturation);
                    existing.duration = ConsumableFood.toDuration(nutrition, saturation);
    
                    common.foodItemConfigurations.put(id, existing);
                    AutoConfig.getConfigHolder(Common.class).save();
                }
                return existing;
            }
            return null;
        }

        @ConfigEntry.Gui.Tooltip
        @Comment("Multiplier applied to the bonus hearts granted by food (default: 1.0)")
        public double foodHeartsMultiplier = 1.0;

        @ConfigEntry.Gui.Tooltip
        @Comment("Multiplier applied to how long food effects last (default: 1.0)")
        public double foodDurationMultiplier = 1.0;

        @ConfigEntry.Gui.Tooltip
        @Comment("Percentage of the smallest active food's duration granted as Nourishment when eating while full (default: 0.1 = 10%)")
        public double nourishmentDurationPercent = 0.1;
        
        @ConfigEntry.Gui.Tooltip(count = 3)
        @Comment("""
            Per-item food data overrides:
            - hearts: flat number of bonus hearts granted by this item
            - duration: flat number of ticks this item's effect lasts
        """)
        public Map<String, FoodItemConfiguration> foodItemConfigurations = new LinkedHashMap<>();

        public static final class FoodItemConfiguration implements ConfigData {
            public int hearts;
            public int duration;
        }
    }
}