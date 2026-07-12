package com.than00ber.renourisheddelight.config;

import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.data.FoodPresetRegistry;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ConfigUtil {

    public static void mergePresets(List<Configuration.FoodItemEntry> entries) {
        for (Configuration.FoodItemEntry preset : FoodPresetRegistry.all()) {
            if (preset.item.isEmpty()) continue;
            Configuration.FoodItemEntry match = entries.stream().filter(x -> preset.item.equals(x.item)).findFirst().orElse(null);

            if (preset.override) {
                if (match == null) {
                    match = new Configuration.FoodItemEntry();
                    match.item = preset.item;
                    entries.add(match);
                }
                match.attributes = copyOfBonuses(preset.attributes);
            } else if (match == null) {
                Configuration.FoodItemEntry entry = new Configuration.FoodItemEntry();
                entry.item = preset.item;
                entry.attributes = copyOfBonuses(preset.attributes);
                entries.add(entry);
            } else {
                for (Configuration.AttributeBonus bonus : preset.attributes) {
                    boolean present = match.attributes.stream().anyMatch(x -> x.attribute.equals(bonus.attribute));
                    if (!present) {
                        match.attributes.add(copyOf(bonus));
                    }
                }
            }
        }
    }

    public static @Nullable Configuration.FoodItemEntry findEntry(List<Configuration.FoodItemEntry> entries, String id) {
        return entries.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);
    }

    public static Configuration.FoodItemEntry seedEntry(List<Configuration.FoodItemEntry> entries, Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        Configuration.FoodItemEntry preset = FoodPresetRegistry.get(id);
        List<Configuration.AttributeBonus> seed = preset != null && !preset.attributes.isEmpty()
                ? copyOfBonuses(preset.attributes)
                : new ArrayList<>(List.of(computeGenericDefault(item)));

        Configuration.FoodItemEntry entry = new Configuration.FoodItemEntry();
        entry.item = id;
        entry.attributes = seed;
        entries.add(entry);
        return entry;
    }

    public static Configuration.AttributeBonus computeGenericDefault(Item item) {
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        int nutrition = properties != null ? properties.nutrition() : 2;
        float saturation = properties != null ? properties.saturation() : 0.0F;

        return new Configuration.AttributeBonus(
                Attributes.MAX_HEALTH.getRegisteredName(),
                AttributeModifier.Operation.ADD_VALUE.getSerializedName(),
                Math.max(1, ConsumableFoodInstance.toHearts(nutrition, saturation)),
                ConsumableFoodInstance.toDuration(nutrition, saturation));
    }

    public static List<Configuration.FoodItemEntry> copyOf(List<Configuration.FoodItemEntry> source) {
        List<Configuration.FoodItemEntry> copy = new ArrayList<>();
        for (Configuration.FoodItemEntry entry : source) {
            Configuration.FoodItemEntry clone = new Configuration.FoodItemEntry();
            clone.item = entry.item;
            clone.override = entry.override;
            clone.attributes = copyOfBonuses(entry.attributes);
            copy.add(clone);
        }
        return copy;
    }

    private static List<Configuration.AttributeBonus> copyOfBonuses(List<Configuration.AttributeBonus> source) {
        List<Configuration.AttributeBonus> copy = new ArrayList<>();
        for (Configuration.AttributeBonus bonus : source) {
            copy.add(copyOf(bonus));
        }
        return copy;
    }

    private static Configuration.AttributeBonus copyOf(Configuration.AttributeBonus bonus) {
        return new Configuration.AttributeBonus(bonus.attribute, bonus.operation, bonus.amount, bonus.duration);
    }
}
