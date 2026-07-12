package com.than00ber.renourisheddelight.data;

import com.than00ber.renourisheddelight.Configuration;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FoodPresetRegistry {

    private static Map<String, Configuration.FoodItemEntry> presets = Map.of();

    public static void set(List<Configuration.FoodItemEntry> entries) {
        Map<String, Configuration.FoodItemEntry> map = new HashMap<>();

        for (Configuration.FoodItemEntry entry : entries) {
            if (!entry.item.isEmpty()) {
                map.put(entry.item, entry);
            }
        }
        presets = map;
    }

    public static @Nullable Configuration.FoodItemEntry get(String id) {
        return presets.get(id);
    }

    public static List<Configuration.FoodItemEntry> all() {
        return List.copyOf(presets.values());
    }
}
