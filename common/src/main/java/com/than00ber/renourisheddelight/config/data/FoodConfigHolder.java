package com.than00ber.renourisheddelight.config.data;

import java.util.List;

public interface FoodConfigHolder {

    List<FoodItemEntry> getFoodConfig();

    void setFoodConfig(List<FoodItemEntry> entries);
}
