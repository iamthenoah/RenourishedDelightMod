package com.than00ber.renourisheddelight.config;

import com.than00ber.renourisheddelight.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.FoodPresetRegistry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
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

    public static void mergePresets(List<FoodItemEntry> entries) {
        for (FoodItemEntry preset : FoodPresetRegistry.all()) {
            if (preset.item.isEmpty()) continue;
            FoodItemEntry match = entries.stream().filter(x -> preset.item.equals(x.item)).findFirst().orElse(null);

            if (preset.override) {
                if (match == null) {
                    match = new FoodItemEntry();
                    match.item = preset.item;
                    entries.add(match);
                }
                match.attributes = copyOfBonuses(preset.attributes);
            } else if (match == null) {
                FoodItemEntry entry = new FoodItemEntry();
                entry.item = preset.item;
                entry.attributes = copyOfBonuses(preset.attributes);
                entries.add(entry);
            } else {
                for (AttributeBonus bonus : preset.attributes) {
                    boolean present = match.attributes.stream().anyMatch(x -> x.attribute.equals(bonus.attribute));
                    if (!present) {
                        match.attributes.add(copyOf(bonus));
                    }
                }
            }
        }
    }

    public static @Nullable FoodItemEntry findEntry(List<FoodItemEntry> entries, String id) {
        return entries.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);
    }

    public static FoodItemEntry seedEntry(List<FoodItemEntry> entries, Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        FoodItemEntry preset = FoodPresetRegistry.get(id);
        List<AttributeBonus> seed = preset != null && !preset.attributes.isEmpty()
                ? copyOfBonuses(preset.attributes)
                : new ArrayList<>(List.of(computeGenericDefault(item)));

        FoodItemEntry entry = new FoodItemEntry();
        entry.item = id;
        entry.attributes = seed;
        entries.add(entry);
        return entry;
    }

    public static AttributeBonus computeGenericDefault(Item item) {
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        int nutrition = properties != null ? properties.nutrition() : 2;
        float saturation = properties != null ? properties.saturation() : 0.0F;

        return new AttributeBonus(
                Attributes.MAX_HEALTH.getRegisteredName(),
                AttributeModifier.Operation.ADD_VALUE.getSerializedName(),
                Math.max(1, ConsumableFoodInstance.toHearts(nutrition, saturation)),
                ConsumableFoodInstance.toDuration(nutrition, saturation));
    }

    public static List<FoodItemEntry> copyOf(List<FoodItemEntry> source) {
        List<FoodItemEntry> copy = new ArrayList<>();
        for (FoodItemEntry entry : source) {
            FoodItemEntry clone = new FoodItemEntry();
            clone.item = entry.item;
            clone.override = entry.override;
            clone.attributes = copyOfBonuses(entry.attributes);
            copy.add(clone);
        }
        return copy;
    }

    private static List<AttributeBonus> copyOfBonuses(List<AttributeBonus> source) {
        List<AttributeBonus> copy = new ArrayList<>();
        for (AttributeBonus bonus : source) {
            copy.add(copyOf(bonus));
        }
        return copy;
    }

    private static AttributeBonus copyOf(AttributeBonus bonus) {
        return new AttributeBonus(bonus.attribute, bonus.operation, bonus.amount, bonus.duration);
    }
}
