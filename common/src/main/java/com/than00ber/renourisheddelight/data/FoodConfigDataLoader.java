package com.than00ber.renourisheddelight.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FoodConfigDataLoader extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "presets");
    private static final String DIRECTORY = "presets";

    public FoodConfigDataLoader() {
        super(new Gson(), DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Configuration.FoodItemEntry> entries = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> resource : resources.entrySet()) {
            if (!resource.getValue().isJsonArray()) continue;

            for (JsonElement element : resource.getValue().getAsJsonArray()) {
                if (element.isJsonObject()) {
                    entries.add(parseEntry(element.getAsJsonObject()));
                }
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
