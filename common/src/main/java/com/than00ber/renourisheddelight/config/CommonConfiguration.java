package com.than00ber.renourisheddelight.config;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.data.DurationMultiplierEntry;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.config.data.FoodPresetRegistry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import dev.architectury.event.events.common.LifecycleEvent;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

@Config(name = RenourishedDelightMod.MOD_ID + "/common")
public final class CommonConfiguration implements ConfigData {

    public static void init() {
        AutoConfig.register(CommonConfiguration.class, JanksonConfigSerializer::new);
        LifecycleEvent.SETUP.register(getInstance()::populateDefaults);
    }

    public static CommonConfiguration getInstance() {
        return AutoConfig.getConfigHolder(CommonConfiguration.class).getConfig();
    }

    public boolean hasConfiguredEntry(Object value) {
        String id = value instanceof Item item
                ? BuiltInRegistries.ITEM.getKey(item).toString()
                : value.toString();
        return FoodPresetRegistry.getInstance().get(id) != null || foodItemConfigurations.stream().anyMatch(x -> id.equals(x.item));
    }

    private void populateDefaults() {
        boolean added = populateFoodItemDefaults();
        added |= populateMissingDurationMultipliers();

        if (added) {
            AutoConfig.getConfigHolder(CommonConfiguration.class).save();
        }
    }

    private boolean populateFoodItemDefaults() {
        boolean added = false;

        for (Item item : BuiltInRegistries.ITEM) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            boolean alreadyListed = foodItemConfigurations.stream().anyMatch(x -> id.equals(x.item));
            if (alreadyListed) continue;

            boolean isFood = item.components().get(DataComponents.FOOD) != null;
            boolean hasPreset = FoodPresetRegistry.getInstance().get(id) != null;

            if (isFood || hasPreset) {
                foodItemConfigurations.add(new FoodItemEntry(id, AttributeBonus.computeDefaultBonuses(item)));
                added = true;
            }
        }
        return added;
    }

    public boolean populateMissingDurationMultipliers() {
        boolean added = false;

        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
            boolean alreadyListed = durationMultipliers.stream().anyMatch(entry -> {
                Holder<Attribute> candidate = ConsumableFoodInstance.resolveAttribute(entry.attribute);
                return candidate != null && candidate.value() == holder.value();
            });
            if (alreadyListed) continue;

            String id = BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString();
            durationMultipliers.add(new DurationMultiplierEntry(id, 1.0));
            added = true;
        }
        return added;
    }

    public double getDurationMultiplier(String attributeId) {
        Holder<Attribute> attribute = ConsumableFoodInstance.resolveAttribute(attributeId);

        if (attribute != null) {
            for (DurationMultiplierEntry entry : durationMultipliers) {
                Holder<Attribute> candidate = ConsumableFoodInstance.resolveAttribute(entry.attribute);

                if (candidate != null && candidate.value() == attribute.value()) {
                    return entry.multiplier;
                }
            }
        }
        return 1.0;
    }

    public List<AttributeBonus> getAttributes(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        FoodItemEntry preset = FoodPresetRegistry.getInstance().get(id);
        FoodItemEntry match = foodItemConfigurations.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);

        if (preset != null && preset.override && !preset.attributes.isEmpty()) {
            return preset.attributes;
        }
        if (preset != null && !preset.attributes.isEmpty()) {
            List<AttributeBonus> merged = new ArrayList<>();
            if (match != null) merged.addAll(match.attributes);

            for (AttributeBonus bonus : preset.attributes) {
                if (merged.stream().noneMatch(x -> x.attribute.equals(bonus.attribute))) {
                    merged.add(bonus);
                }
            }
            return merged;
        }
        if (match != null && !match.attributes.isEmpty()) {
            return match.attributes;
        }
        List<AttributeBonus> attributes = new ArrayList<>(List.of(AttributeBonus.computeGenericDefault(item)));

        if (match != null) {
            match.attributes = attributes;
        } else {
            foodItemConfigurations.add(new FoodItemEntry(id, attributes));
        }
        AutoConfig.getConfigHolder(CommonConfiguration.class).save();
        return attributes;
    }

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
    operation can be: add_value, add_multiplied_base, add_multiplied_total
    """)
    public List<FoodItemEntry> foodItemConfigurations = new ArrayList<>();

    @ConfigEntry.Gui.Excluded
    @Comment("""
    Per-attribute duration multipliers, applied when a food item's bonus is actually granted (does not modify the stored per-item durations). Each entry maps an attribute id to a multiplier. Example:
    [
      {
        attribute: "minecraft:generic.max_health",
        multiplier: 1.5,
      }
    ]
    """)
    public List<DurationMultiplierEntry> durationMultipliers = new ArrayList<>();
}
