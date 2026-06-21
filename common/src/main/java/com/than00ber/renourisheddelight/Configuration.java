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

        Common loaded = Common.getInstance();
        System.out.println("[RD] IMMEDIATELY AFTER REGISTER — itemConfigs size: " + loaded.itemConfigs.size());
        loaded.itemConfigs.forEach((k, v) -> System.out.println("[RD]   " + k + " = " + v.hearts + "/" + v.duration));
    }

    public static Common.ItemConfig getItemConfig(Item item) {
        if (!item.components().has(DataComponents.FOOD)) {
            return null;
        }

        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        Common common = Common.getInstance();

        System.out.println("[RD] getItemConfig(" + id + ") — current itemConfigs size: " + common.itemConfigs.size());
        Common.ItemConfig existing = common.itemConfigs.get(id);
        System.out.println("[RD] existing for " + id + " = " + (existing == null ? "NULL" : existing.hearts + "/" + existing.duration));

        if (existing == null) {
            FoodProperties properties = item.components().get(DataComponents.FOOD);
            int nutrition = properties != null ? properties.nutrition() : 2;
            float saturation = properties != null ? properties.saturation() : 0.0F;

            existing = new Common.ItemConfig();
            existing.hearts = ConsumableFood.toHearts(nutrition);
            existing.duration = ConsumableFood.toDuration(nutrition, saturation);

            System.out.println("[RD] CREATING new default entry for " + id + " = " + existing.hearts + "/" + existing.duration);

            common.itemConfigs.put(id, existing);
            AutoConfig.getConfigHolder(Common.class).save();
        }

        return existing;
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

        @ConfigEntry.Gui.Tooltip
        @Comment("Multiplier applied to the bonus hearts granted by food (default: 1.0)")
        public double foodHeartsMultiplier = 1.0;

        @ConfigEntry.Gui.Tooltip
        @Comment("Multiplier applied to how long food effects last (default: 1.0)")
        public double foodDurationMultiplier = 1.0;

        @ConfigEntry.Gui.Tooltip(count = 3)
        @Comment("""
            Per-item food overrides (auto-generated as items are eaten):
            - hearts: flat number of bonus hearts granted by this item
            - duration: flat number of ticks this item's effect lasts
        """)
        public Map<String, ItemConfig> itemConfigs = new LinkedHashMap<>();

        public static final class ItemConfig implements ConfigData {
            public int hearts;
            public int duration;
        }
    }
}