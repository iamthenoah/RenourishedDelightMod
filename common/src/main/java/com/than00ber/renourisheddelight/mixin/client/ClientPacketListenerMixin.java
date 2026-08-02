package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements FoodConfigHolder {

    @Unique
    private final List<FoodItemEntry> renourisheddelight$foodConfig = new ArrayList<>();

    @Override
    public List<FoodItemEntry> getFoodConfig() {
        return renourisheddelight$foodConfig;
    }

    @Override
    public void setFoodConfig(List<FoodItemEntry> entries) {
        renourisheddelight$foodConfig.clear();
        entries.forEach(x -> renourisheddelight$foodConfig.add(x.copy()));
    }
}
