package com.than00ber.renourisheddelight.config;

import com.google.common.collect.Lists;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.data.DurationMultiplierEntry;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Config(name = RenourishedDelightMod.MOD_ID + "/common")
public final class CommonConfiguration implements ConfigData {

    public static void init() {
        AutoConfig.register(CommonConfiguration.class, JanksonConfigSerializer::new);
        LifecycleEvent.SETUP.register(getInstance()::populateDefaults);
    }

    public static CommonConfiguration getInstance() {
        return AutoConfig.getConfigHolder(CommonConfiguration.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(CommonConfiguration.class).save();
    }

    @ConfigEntry.Gui.Excluded
    @Comment("""
    Per-item attribute bonuses used as the starting point for newly created worlds. Each entry is an item id plus a list of bonuses, and each bonus has its own duration (in ticks, 20 = 1 second). Example:
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
    Editing this has no effect on worlds that already exist; use the in-game config screen for those.
    """)
    public List<FoodItemEntry> foodItemConfigurations = new ArrayList<>();

    @ConfigEntry.Gui.Excluded
    @Comment("""
    Per-attribute duration multipliers, applied when a food item's bonus is actually granted. Each entry maps an attribute id to a multiplier. Example:
    [
      {
        attribute: "minecraft:generic.max_health",
        multiplier: 1.5,
      }
    ]
    """)
    public List<DurationMultiplierEntry> durationMultipliers = new ArrayList<>();

    public double getDurationMultiplier(String attributeId) {
        return Optional.ofNullable(findDurationMultiplierEntry(attributeId)).map(x -> x.multiplier).orElse(1.0);
    }

    private @Nullable DurationMultiplierEntry findDurationMultiplierEntry(String attributeId) {
        Holder<Attribute> attribute = ConsumableFoodInstance.resolveAttribute(attributeId);

        if (attribute != null) {
            for (DurationMultiplierEntry entry : durationMultipliers) {
                Holder<Attribute> candidate = ConsumableFoodInstance.resolveAttribute(entry.attribute);

                if (candidate != null && candidate.value() == attribute.value()) {
                    return entry;
                }
            }
        }
        return null;
    }

    private void populateDefaults() {
        if (populateFoodItemDefaults() | populateDurationMultiplierDefaults()) save();
    }

    private boolean populateFoodItemDefaults() {
        return populateMissing(BuiltInRegistries.ITEM,
                x -> BuiltInRegistries.ITEM.getKey(x).toString(),
                id -> FoodItemEntry.get(foodItemConfigurations, id) != null,
                x -> x.components().get(DataComponents.FOOD) != null,
                (id, x) -> foodItemConfigurations.add(new FoodItemEntry(id, Lists.newArrayList(AttributeBonus.defaultMaxHealth(x)))));
    }

    public boolean populateDurationMultiplierDefaults() {
        return populateMissing(BuiltInRegistries.ATTRIBUTE,
                x -> Optional.ofNullable(BuiltInRegistries.ATTRIBUTE.getKey(x)).map(ResourceLocation::toString).orElse(""),
                id -> findDurationMultiplierEntry(id) != null,
                x -> true,
                (id, x) -> durationMultipliers.add(new DurationMultiplierEntry(id, 1.0)));
    }

    private static <T> boolean populateMissing(Iterable<T> universe, Function<T, String> idOf, Predicate<String> alreadyListed, Predicate<T> include, BiConsumer<String, T> add) {
        boolean added = false;

        for (T value : universe) {
            String id = idOf.apply(value);

            if (!alreadyListed.test(id) && include.test(value)) {
                add.accept(id, value);
                added = true;
            }
        }
        return added;
    }
}
