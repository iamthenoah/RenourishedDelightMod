package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.network.FoodConfigSyncPayload;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements FoodConfigHolder {

    @Unique private final FoodConfig renourisheddelight$config = new FoodConfig();
    @Unique private List<FoodItemEntry> renourisheddelight$presets = List.of();

    @Override
    public FoodConfig getFoodConfig() {
        return renourisheddelight$config;
    }

    // the server owns the world config; it applies the edit and syncs it back through sync()
    @Override
    public void setFoodConfig(FoodConfig config) {
        new FoodConfigSyncPayload.Edit(config).sendToServer();
    }

    @Override
    public List<FoodItemEntry> getPresets() {
        return renourisheddelight$presets;
    }

    @Override
    public void sync(FoodConfig config, List<FoodItemEntry> presets) {
        renourisheddelight$config.copyFrom(config);
        renourisheddelight$presets = presets.stream().map(FoodItemEntry::copy).toList();
    }
}
