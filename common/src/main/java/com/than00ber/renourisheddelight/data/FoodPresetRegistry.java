package com.than00ber.renourisheddelight.data;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FoodPresetRegistry {

    private static Map<String, FoodItemEntry> presets = Map.of();

    public static void set(List<FoodItemEntry> entries) {
        Map<String, FoodItemEntry> map = new HashMap<>();

        for (FoodItemEntry entry : entries) {
            if (!entry.item.isEmpty()) {
                map.put(entry.item, entry);
            }
        }
        presets = map;
    }

    public static @Nullable FoodItemEntry get(String id) {
        return presets.get(id);
    }

    public static List<FoodItemEntry> all() {
        return List.copyOf(presets.values());
    }
}
