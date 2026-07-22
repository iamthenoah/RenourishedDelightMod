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
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
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

        if (!Files.exists(Utils.getConfigFolder().resolve(RenourishedDelightMod.MOD_ID + "/common.json5"))) {
            LifecycleEvent.SETUP.register(getInstance()::populateDefaults);
        }
    }

    public static CommonConfiguration getInstance() {
        return AutoConfig.getConfigHolder(CommonConfiguration.class).getConfig();
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
    Per-attribute duration multipliers, applied when a food item's bonus is actually granted. Each entry maps an attribute id to a multiplier. Example:
    [
      {
        attribute: "minecraft:generic.max_health",
        multiplier: 1.5,
      }
    ]
    """)
    public List<DurationMultiplierEntry> durationMultipliers = new ArrayList<>();

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
    
    public boolean hasFoodItemEntry(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        FoodItemEntry entry = FoodPresetRegistry.getInstance().get(id);
        return entry != null || foodItemConfigurations.stream().anyMatch(x -> id.equals(x.item));
    }

    public double getDurationMultiplier(String attributeId) {
        DurationMultiplierEntry entry = findDurationMultiplierEntry(attributeId);
        return entry != null ? entry.multiplier : 1.0;
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
        if (populateFoodItemDefaults() || populateDurationMultiplierDefaults()) {
            AutoConfig.getConfigHolder(CommonConfiguration.class).save();
        }
    }
    
    private boolean populateFoodItemDefaults() {
        return populateMissing(BuiltInRegistries.ITEM,
                x -> BuiltInRegistries.ITEM.getKey(x).toString(),
                id -> foodItemConfigurations.stream().anyMatch(x -> id.equals(x.item)),
                x -> x.components().get(DataComponents.FOOD) != null || FoodPresetRegistry.getInstance().get(BuiltInRegistries.ITEM.getKey(x).toString()) != null,
                (id, x) -> foodItemConfigurations.add(new FoodItemEntry(id, AttributeBonus.computeDefaultBonuses(x))));
    }

    public boolean populateDurationMultiplierDefaults() {
        return populateMissing(BuiltInRegistries.ATTRIBUTE,
                x -> Optional.ofNullable(BuiltInRegistries.ATTRIBUTE.getKey(x)).map(ResourceLocation::toString).orElse(""),
                id -> findDurationMultiplierEntry(id) != null,
                x -> true,
                (id, x) -> durationMultipliers.add(new DurationMultiplierEntry(id, 1.0)));
    }

    private static  <T> boolean populateMissing(Iterable<T> universe, Function<T, String> idOf, Predicate<String> alreadyListed, Predicate<T> include, BiConsumer<String, T> add) {
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
