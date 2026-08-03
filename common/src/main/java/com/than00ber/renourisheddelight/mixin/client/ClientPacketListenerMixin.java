package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.config.data.DurationMultiplierEntry;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements FoodConfigHolder {

    @Unique private final List<FoodItemEntry> renourisheddelight$foodConfig = new ArrayList<>();
    @Unique private final List<DurationMultiplierEntry> renourisheddelight$multiplierConfig = new ArrayList<>();

    @Override
    public List<FoodItemEntry> getFoodConfig() {
        return renourisheddelight$foodConfig;
    }

    @Override
    public List<DurationMultiplierEntry> getMultiplierConfig() {
        return renourisheddelight$multiplierConfig;
    }

    @Override
    public void update(List<FoodItemEntry> entries, List<DurationMultiplierEntry> multipliers) {
        renourisheddelight$foodConfig.clear();
        renourisheddelight$multiplierConfig.clear();
        entries.forEach(x -> renourisheddelight$foodConfig.add(x.copy()));
        multipliers.forEach(x -> renourisheddelight$multiplierConfig.add(x.copy()));
    }
}
