package com.than00ber.renourisheddelight.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FoodPresetRegistry {

    private static Map<String, FoodItemEntry> presets = Map.of();

    public static void set(List<FoodItemEntry> entries) {
        presets = entries.stream()
                .filter(x -> !x.item.isEmpty())
                .collect(Collectors.toMap(x -> x.item, Function.identity()));
    }

    public static @Nullable FoodItemEntry get(String id) {
        return presets.get(id);
    }

    public static List<FoodItemEntry> all() {
        return List.copyOf(presets.values());
    }
}
