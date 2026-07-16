package com.than00ber.renourisheddelight.config.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FoodPresetRegistry {

    private static final FoodPresetRegistry INSTANCE = new FoodPresetRegistry();

    public static void init() {
        getInstance().set(FoodConfigDataLoader.loadBuiltinPresets());
    }

    public static FoodPresetRegistry getInstance() {
        return INSTANCE;
    }

    private Map<String, FoodItemEntry> presets = Map.of();
    
    public void set(List<FoodItemEntry> entries) {
        presets = entries.stream()
                .filter(x -> !x.item.isEmpty())
                .collect(Collectors.toMap(x -> x.item, Function.identity(), (f, s) -> s));
    }

    public @Nullable FoodItemEntry get(String id) {
        return presets.get(id);
    }
}