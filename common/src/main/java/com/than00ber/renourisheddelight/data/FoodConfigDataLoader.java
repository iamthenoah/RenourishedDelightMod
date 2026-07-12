package com.than00ber.renourisheddelight.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
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

    public static void init() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new FoodConfigDataLoader(), FoodConfigDataLoader.ID);
    }

    public FoodConfigDataLoader() {
        super(new Gson(), DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<FoodItemEntry> entries = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> resource : resources.entrySet()) {
            if (!resource.getValue().isJsonArray()) continue;

            for (JsonElement element : resource.getValue().getAsJsonArray()) {
                if (element.isJsonObject()) {
                    entries.add(toFoodItemEntry(element.getAsJsonObject()));
                }
            }
        }
        FoodPresetRegistry.getInstance().set(entries);
        RenourishedDelightMod.LOGGER.info("Loaded {} preset food entries from {} data file(s)", entries.size(), resources.size());
    }

    private FoodItemEntry toFoodItemEntry(JsonObject object) {
        FoodItemEntry entry = new FoodItemEntry(
                GsonHelper.getAsString(object, "item", ""),
                new ArrayList<>(),
                GsonHelper.getAsBoolean(object, "override", false));

        for (JsonElement element : GsonHelper.getAsJsonArray(object, "attributes", new JsonArray())) {
            if (element.isJsonObject()) {
                JsonObject bonus = element.getAsJsonObject();

                entry.attributes.add(new AttributeBonus(
                        GsonHelper.getAsString(bonus, "attribute", ""),
                        GsonHelper.getAsString(bonus, "operation", "add_value"),
                        GsonHelper.getAsDouble(bonus, "amount", 0.0),
                        GsonHelper.getAsInt(bonus, "duration", 0)));
            }
        }
        return entry;
    }
}
