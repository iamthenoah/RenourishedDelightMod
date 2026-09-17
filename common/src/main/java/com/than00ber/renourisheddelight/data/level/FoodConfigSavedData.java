package com.than00ber.renourisheddelight.data.level;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.FoodConfigReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
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
        SavedData.Factory<FoodConfigSavedData> factory = new SavedData.Factory<>(
                () -> new FoodConfigSavedData(new FoodConfig(CommonConfiguration.getInstance().getFoodConfig())),
                (tag, provider) -> new FoodConfigSavedData(FoodConfig.load(tag)),
                DataFixTypes.LEVEL);
        return server.overworld().getDataStorage().computeIfAbsent(factory, ID);
    }

    @Override
    public FoodConfig getFoodConfig() {
        return config;
    }

    @Override
    public void setFoodConfig(FoodConfig updated) {
        config.copyFrom(updated);
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
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        return config.save(tag);
    }
}
