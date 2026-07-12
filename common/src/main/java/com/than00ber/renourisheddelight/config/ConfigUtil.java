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
import java.util.stream.Collectors;

public final class ConfigUtil {

    public static void mergePresets(List<FoodItemEntry> entries) {
        for (FoodItemEntry preset : FoodPresetRegistry.all()) {
            if (!preset.item.isEmpty()) {
                FoodItemEntry match = entries.stream()
                        .filter(x -> preset.item.equals(x.item))
                        .findFirst()
                        .orElse(null);

                if (match != null) {
                    if (!preset.override) {
                        for (AttributeBonus bonus : preset.attributes) {
                            if (match.attributes.stream().noneMatch(x -> x.attribute.equals(bonus.attribute))) {
                                match.attributes.add(bonus.copy());
                            }
                        }
                    }
                } else {
                    entries.add(preset.copy());
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
                ? preset.attributes.stream().map(AttributeBonus::copy).collect(Collectors.toList()) 
                : new ArrayList<>(List.of(computeGenericDefault(item)));

        FoodItemEntry entry = new FoodItemEntry(id, seed);
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
}
