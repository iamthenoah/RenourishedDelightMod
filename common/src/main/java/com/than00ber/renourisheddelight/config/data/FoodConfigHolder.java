package com.than00ber.renourisheddelight.config.data;

import java.util.List;

public interface FoodConfigHolder {

    List<FoodItemEntry> getFoodConfig();

    List<DurationMultiplierEntry> getMultiplierConfig();

    List<StarvationEntry> getStarvationConfig();

    void update(List<FoodItemEntry> entries, List<DurationMultiplierEntry> multipliers, List<StarvationEntry> starvation);
}
