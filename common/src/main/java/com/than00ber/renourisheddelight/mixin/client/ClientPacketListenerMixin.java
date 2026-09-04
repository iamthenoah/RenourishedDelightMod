package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements FoodConfigHolder {

    @Unique private final FoodConfig renourisheddelight$config = new FoodConfig();

    @Override
    public FoodConfig getFoodConfig() {
        return renourisheddelight$config;
    }

    @Override
    public void setFoodConfig(FoodConfig config) {
        renourisheddelight$config.copyFrom(config);
    }
}
