package com.than00ber.renourisheddelight.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.network.FoodConfigSyncPayload;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.GameInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FoodConfigReloadListener extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation PRESETS_KEY =  RenourishedDelightMod.key("presets");
    public static volatile List<FoodItemEntry> PRESETS = List.of();

    public static void init() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new FoodConfigReloadListener(), PRESETS_KEY);
    }

    public FoodConfigReloadListener() {
        super(new Gson(), "presets");
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
        PRESETS = entries;
        RenourishedDelightMod.LOGGER.info("Loaded {} preset food entries from {} data file(s)", entries.size(), resources.size());
        MinecraftServer server = GameInstance.getServer();

        if (server != null) {
            FoodConfigSavedData config = FoodConfigSavedData.get(server);

            if (config.applyPresets()) {
                NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), FoodConfigSyncPayload.of(config));
            }
        }
    }

    private static FoodItemEntry toFoodItemEntry(JsonObject object) {
        String id = GsonHelper.getAsString(object, "item", "");
        List<AttributeBonus> declared = new ArrayList<>();

        for (JsonElement element : GsonHelper.getAsJsonArray(object, "attributes", new JsonArray())) {
            if (element.isJsonObject()) {
                JsonObject bonus = element.getAsJsonObject();

                declared.add(new AttributeBonus(
                        GsonHelper.getAsString(bonus, "attribute", ""),
                        GsonHelper.getAsString(bonus, "operation", "add_value"),
                        GsonHelper.getAsDouble(bonus, "amount", 0.0),
                        GsonHelper.getAsInt(bonus, "duration", 0)));
            }
        }
        return new FoodItemEntry(id, withDefaults(id, declared));
    }

    private static List<AttributeBonus> withDefaults(String id, List<AttributeBonus> declared) {
        List<AttributeBonus> bonuses = new ArrayList<>();
        Item item = resolveItem(id);

        if (item != null) {
            for (AttributeBonus base : AttributeBonus.defaults(item)) {
                if (declared.stream().noneMatch(x -> x.sameAttribute(base))) bonuses.add(base);
            }
        }
        bonuses.addAll(declared);
        return bonuses;
    }

    private static @Nullable Item resolveItem(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            return item != Items.AIR ? item : null;
        } catch (Exception exception) {
            return null;
        }
    }
}
