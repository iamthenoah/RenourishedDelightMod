package com.than00ber.renourisheddelight;

import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
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
import java.util.List;

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
            if (!item.components().has(DataComponents.FOOD)) return List.of();
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            FoodItemEntry match = null;

            for (FoodItemEntry entry : foodItemConfigurations) {
                if (id.equals(entry.item)) {
                    match = entry;
                    break;
                }
            }
            if (match != null && !match.attributes.isEmpty()) return match.attributes;
            FoodProperties properties = item.components().get(DataComponents.FOOD);
            int nutrition = properties != null ? properties.nutrition() : 2;
            float saturation = properties != null ? properties.saturation() : 0.0F;

            AttributeBonus maxHealth = new AttributeBonus(
                    Attributes.MAX_HEALTH.getRegisteredName(),
                    AttributeModifier.Operation.ADD_VALUE.getSerializedName(),
                    Math.max(1, ConsumableFoodInstance.toHearts(nutrition, saturation)),
                    ConsumableFoodInstance.toDuration(nutrition, saturation));
            List<AttributeBonus> attributes = new ArrayList<>(List.of(maxHealth));

            if (match != null) {
                match.attributes = attributes;
            } else {
                FoodItemEntry entry = new FoodItemEntry();
                entry.item = id;
                entry.attributes = attributes;
                foodItemConfigurations.add(entry);
            }
            AutoConfig.getConfigHolder(Common.class).save();
            return attributes;
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

        @ConfigEntry.Gui.Excluded
        @Comment("""
                Per-item attribute bonuses. Each entry is an item id plus a list of bonuses, and each bonus has its own duration (in ticks, 20 = 1 second). Example:
                [
                  {
                    item: "minecraft:golden_apple",
                    attributes: [
                      {
                        attribute: "minecraft:generic.max_health",
                        operation: "add_value",
                        amount: 4.0,
                        duration: 6000,
                      },
                      {
                        attribute: "minecraft:generic.movement_speed",
                        operation: "add_multiplied_base",
                        amount: 0.2,
                        duration: 2400,
                      }
                    ]
                  }
                ]
                operation can be: add_value, add_multiplied_base, add_multiplied_total""")
        public List<FoodItemEntry> foodItemConfigurations = new ArrayList<>();
    }

    public static final class FoodItemEntry {
        public String item = "";
        public List<AttributeBonus> attributes = new ArrayList<>();
    }

    public static final class AttributeBonus {

        public String attribute;
        public String operation;
        public double amount;
        public int duration;

        public AttributeBonus(String attribute, String operation, double amount, int duration) {
            this.attribute = attribute;
            this.operation = operation;
            this.amount = amount;
            this.duration = duration;
        }

        public String attribute() {
            return attribute;
        }

        public String operation() {
            return operation;
        }

        public double amount() {
            return amount;
        }

        public int duration() {
            return duration;
        }
    }
}
