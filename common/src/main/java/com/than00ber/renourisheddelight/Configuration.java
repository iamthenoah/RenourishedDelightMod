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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

        @ConfigEntry.Gui.Tooltip
        @Comment("Item ID to sample for the golden-effect color palette (default: minecraft:golden_carrot)")
        public String goldenPaletteItem = "minecraft:golden_carrot";

        @ConfigEntry.Gui.Tooltip
        @Comment("Whether to render the active food items panel next to the inventory screen (default: true)")
        public boolean showFoodDisplayInInventory = true;

        @ConfigEntry.Gui.Tooltip
        @Comment("Cache generated item icon atlases to disk, so resource reloads skip re-rendering every icon when nothing changed (default: true)")
        public boolean enableAtlasCache = true;
    }

    @Config(name = RenourishedDelightMod.MOD_ID + "/common")
    public static final class Common implements ConfigData {

        public static Common getInstance() {
            return AutoConfig.getConfigHolder(Common.class).getConfig();
        }

        public List<AttributeBonus> getAttributes(Item item) {
            if (item.components().has(DataComponents.FOOD)) {
                String id = BuiltInRegistries.ITEM.getKey(item).toString();
                Common common = Common.getInstance();
                List<AttributeBonus> existing = common.foodItemConfigurations.getOrDefault(id, new ArrayList<>());
    
                if (existing.isEmpty()) {
                    FoodProperties properties = item.components().get(DataComponents.FOOD);
                    int nutrition = properties != null ? properties.nutrition() : 2;
                    float saturation = properties != null ? properties.saturation() : 0.0F;

                    AttributeBonus maxHealth = new AttributeBonus(
                            Attributes.MAX_HEALTH.getRegisteredName(), 
                            AttributeModifier.Operation.ADD_VALUE.getSerializedName(),
                            Math.max(1, ConsumableFood.toHearts(nutrition, saturation)),
                            ConsumableFood.toDuration(nutrition, saturation));
                    existing.add(maxHealth);

                    common.foodItemConfigurations.put(id, existing);
                    AutoConfig.getConfigHolder(Common.class).save();
                }
                return existing;
            }
            return new ArrayList<>();
        }

        @ConfigEntry.Gui.Tooltip
        @Comment("Multiplier applied to every attribute bonus amount granted by food (default: 1.0)")
        public double foodAttributeBonusMultiplier = 1.0;

        @ConfigEntry.Gui.Tooltip
        @Comment("Multiplier applied to how long food effects last (default: 1.0)")
        public double foodDurationMultiplier = 1.0;

        @ConfigEntry.Gui.Tooltip
        @Comment("Percentage of the smallest active food's duration granted as Nourishment when eating while full (default: 0.1 = 10%)")
        public double nourishmentDurationPercent = 0.1;

        @ConfigEntry.Gui.Tooltip
        @Comment("Multiplier applied to the natural regen tick interval computed from food quality (default: 1.0)")
        public double regenIntervalMultiplier = 1.0;
        
        @ConfigEntry.Gui.Tooltip(count = 3)
        @Comment("""
            Per-item food data overrides. Key = item registry id, e.g. "minecraft:cooked_beef".
            Each entry holds a list of attribute bonuses granted while that item's effect is active.

            Each attribute bonus has:
              attribute: registry id of the attribute to modify, e.g. "minecraft:generic.max_health",
                         "minecraft:generic.movement_speed", "minecraft:generic.attack_damage"
                         (modded attributes work too - most vanilla ones live under "generic.")
              operation: "add_value", "add_multiplied_base", or "add_multiplied_total"
                         (same semantics as vanilla attribute modifiers)
              amount:    how much to add, in the units that attribute normally uses
                         (max_health uses half-hearts, movement_speed is a fraction of the base speed, etc.)
              duration:  ticks (20 per second) this bonus lasts once the item is eaten -
                         each bonus on the same item can have its own duration

            Example - an apple that grants +2 hearts for 10 minutes and +10% movement speed for 2 minutes:
              "minecraft:apple": [
                {
                  "attribute": "minecraft:generic.max_health",
                  "operation": "add_value",
                  "amount": 4.0,
                  "duration": 12000
                },
                {
                  "attribute": "minecraft:generic.movement_speed",
                  "operation": "add_multiplied_base",
                  "amount": 0.1,
                  "duration": 2400
                }
              ]
        """)
        public Map<String, List<AttributeBonus>> foodItemConfigurations = new LinkedHashMap<>();

        public record AttributeBonus(String attribute, String operation, double amount, int duration) implements ConfigData {
            // do nothing
        }
    }
}