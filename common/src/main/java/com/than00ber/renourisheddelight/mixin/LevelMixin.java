package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin implements FoodConfigHolder {

    @Unique
    private static final List<FoodItemEntry> renourisheddelight$foodConfig = new ArrayList<>();

    @Override
    public List<FoodItemEntry> getFoodConfig() {
        return renourisheddelight$foodConfig;
    }

    @Override
    public void setFoodConfig(List<FoodItemEntry> entries) {
        renourisheddelight$foodConfig.clear();
        renourisheddelight$foodConfig.addAll(entries);
    }
}
