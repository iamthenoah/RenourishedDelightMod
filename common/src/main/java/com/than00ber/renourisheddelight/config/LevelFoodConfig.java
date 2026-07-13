package com.than00ber.renourisheddelight.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.FoodPresetRegistry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LevelFoodConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<FoodItemEntry>>() {}.getType();
    private static final LevelFoodConfig INSTANCE = new LevelFoodConfig();

    public static void init() {
        getInstance().cache.clear();
    }

    public static LevelFoodConfig getInstance() {
        return INSTANCE;
    }

    private final Map<Path, List<FoodItemEntry>> cache = new HashMap<>();

    public @Nullable Path resolveFile(@Nullable MinecraftServer server) {
        return server == null ? null : server.getWorldPath(LevelResource.ROOT)
                .resolve(RenourishedDelightMod.MOD_ID)
                .resolve("food_items.json");
    }

    public List<FoodItemEntry> resolveEntries(Path file) {
        return cache.computeIfAbsent(file, x -> {
            List<FoodItemEntry> entries = read(x);

            if (entries == null) {
                entries = CommonConfiguration.getInstance().foodItemConfigurations.stream()
                        .map(FoodItemEntry::copy)
                        .collect(Collectors.toList());
            }
            mergePresets(entries);
            save(x, entries);
            return entries;
        });
    }

    public void save(Path file, List<FoodItemEntry> entries) {
        try {
            cache.put(file, entries);
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(entries));
        } catch (IOException exception) {
            RenourishedDelightMod.LOGGER.warn("Failed to save per-world food config to {}", file, exception);
        }
    }

    private @Nullable List<FoodItemEntry> read(Path file) {
        if (Files.isRegularFile(file)) {
            try {
                String content = Files.readString(file);
                if (content.isBlank()) return null;
                List<FoodItemEntry> entries = GSON.fromJson(content, LIST_TYPE);
                return entries != null ? entries : new ArrayList<>();
            } catch (IOException | JsonParseException exception) {
                RenourishedDelightMod.LOGGER.warn("Failed to read per-world food config from {}, resetting it", file, exception);
            }
        }
        return null;
    }

    private static void mergePresets(List<FoodItemEntry> entries) {
        for (FoodItemEntry preset : FoodPresetRegistry.getInstance().all()) {
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
}