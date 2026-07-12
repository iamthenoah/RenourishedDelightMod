package com.than00ber.renourisheddelight.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.ConfigUtil;
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

public final class LevelFoodConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Configuration.FoodItemEntry>>() {}.getType();
    private static final Map<Path, List<Configuration.FoodItemEntry>> CACHE = new HashMap<>();

    public static @Nullable Path resolveFile(@Nullable MinecraftServer server) {
        return server == null ? null : server.getWorldPath(LevelResource.ROOT)
                .resolve(RenourishedDelightMod.MOD_ID)
                .resolve("food_items.json");
    }

    public static List<Configuration.FoodItemEntry> resolveEntries(Path file) {
        return CACHE.computeIfAbsent(file, LevelFoodConfig::loadMerged);
    }

    private static List<Configuration.FoodItemEntry> loadMerged(Path file) {
        List<Configuration.FoodItemEntry> entries = read(file);
        if (entries == null) entries = ConfigUtil.copyOf(Configuration.Common.getInstance().foodItemConfigurations);
        ConfigUtil.mergePresets(entries);
        save(file, entries);
        return entries;
    }

    public static void save(Path file, List<Configuration.FoodItemEntry> entries) {
        CACHE.put(file, entries);

        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(entries));
        } catch (IOException exception) {
            RenourishedDelightMod.LOGGER.warn("Failed to save per-world food config to {}", file, exception);
        }
    }

    private static @Nullable List<Configuration.FoodItemEntry> read(Path file) {
        if (!Files.isRegularFile(file)) return null;

        try {
            String content = Files.readString(file);
            List<Configuration.FoodItemEntry> entries = GSON.fromJson(content, LIST_TYPE);
            return entries != null ? entries : new ArrayList<>();
        } catch (IOException exception) {
            RenourishedDelightMod.LOGGER.warn("Failed to read per-world food config from {}", file, exception);
            return null;
        }
    }
}
