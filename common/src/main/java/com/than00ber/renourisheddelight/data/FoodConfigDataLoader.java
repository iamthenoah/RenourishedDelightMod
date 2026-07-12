package com.than00ber.renourisheddelight.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FoodConfigDataLoader implements ResourceManagerReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "presets");
    private static final String DIRECTORY = "presets";
    private static final FoodConfigDataLoader INSTANCE = new FoodConfigDataLoader();

    public static FoodConfigDataLoader getInstance() {
        return INSTANCE;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        List<Configuration.FoodItemEntry> entries = new ArrayList<>();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> resource : resources.entrySet()) {
            try (Reader reader = resource.getValue().openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonArray()) continue;

                for (JsonElement element : root.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        entries.add(parseEntry(element.getAsJsonObject()));
                    }
                }
            } catch (Exception exception) {
                RenourishedDelightMod.LOGGER.warn("Failed to parse preset file {}", resource.getKey(), exception);
            }
        }
        FoodPresetRegistry.set(entries);
        RenourishedDelightMod.LOGGER.info("Loaded {} preset food entries from {} data file(s)", entries.size(), resources.size());
    }

    private static Configuration.FoodItemEntry parseEntry(JsonObject object) {
        Configuration.FoodItemEntry entry = new Configuration.FoodItemEntry();
        entry.item = GsonHelper.getAsString(object, "item", "");
        entry.override = GsonHelper.getAsBoolean(object, "override", false);
        entry.attributes = new ArrayList<>();

        for (JsonElement element : GsonHelper.getAsJsonArray(object, "attributes", new JsonArray())) {
            if (!element.isJsonObject()) continue;
            JsonObject bonus = element.getAsJsonObject();
            entry.attributes.add(new Configuration.AttributeBonus(
                    GsonHelper.getAsString(bonus, "attribute", ""),
                    GsonHelper.getAsString(bonus, "operation", "add_value"),
                    GsonHelper.getAsDouble(bonus, "amount", 0.0),
                    GsonHelper.getAsInt(bonus, "duration", 0)));
        }
        return entry;
    }
}
