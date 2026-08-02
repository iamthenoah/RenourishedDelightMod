package com.than00ber.renourisheddelight.config.data;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.List;

public interface FoodConfigHolder {

    List<FoodItemEntry> getFoodConfig();

    void setFoodConfig(List<FoodItemEntry> entries);

    default FoodItemEntry getFoodItemEntry(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();

        for (FoodItemEntry entry : getFoodConfig()) {
            if (id.equals(entry.item)) return entry;
        }
        return CommonConfiguration.getInstance().getFoodItemEntry(item);
    }

    default boolean hasFoodItemEntry(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        return getFoodConfig().stream().anyMatch(x -> id.equals(x.item));
    }
}
