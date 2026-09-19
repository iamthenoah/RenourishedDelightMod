package com.than00ber.renourisheddelight.config.data;

import com.than00ber.renourisheddelight.data.FoodConfigReloadListener;

import java.util.List;

public interface FoodConfigHolder {

    FoodConfig getFoodConfig();

    void setFoodConfig(FoodConfig config);

    default List<FoodItemEntry> getPresets() {
        return FoodConfigReloadListener.PRESETS;
    }

    default void setPresets(List<FoodItemEntry> presets) {
        // do nothing
    }
}
