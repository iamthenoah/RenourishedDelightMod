package com.than00ber.renourisheddelight.data.level;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.FoodConfigReloadListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public final class FoodConfigSavedData extends SavedData implements FoodConfigHolder {

    private static final String ID = "renourisheddelight_food_config";

    private final FoodConfig config;

    private FoodConfigSavedData(FoodConfig config) {
        this.config = config;
        applyPresets();
    }

    public static FoodConfigSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                tag -> new FoodConfigSavedData(FoodConfig.load(tag)),
                () -> new FoodConfigSavedData(new FoodConfig(CommonConfiguration.getInstance().getFoodConfig())),
                ID);
    }

    @Override
    public FoodConfig getFoodConfig() {
        return config;
    }

    @Override
    public void setFoodConfig(FoodConfig updated) {
        config.copyFrom(updated);
        applyPresets();
        setDirty();
    }

    public boolean applyPresets() {
        boolean changed = false;

        for (FoodItemEntry preset : FoodConfigReloadListener.PRESETS) {
            if (FoodItemEntry.get(config.foods, preset.item) == null) {
                config.foods.add(preset.copy());
                changed = true;
            }
        }
        if (changed) setDirty();
        return changed;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        return config.save(tag);
    }
}
