package com.than00ber.renourisheddelight;

import com.mojang.logging.LogUtils;
import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.data.FoodConfigDataLoader;
import com.than00ber.renourisheddelight.data.FoodPresetRegistry;
import com.than00ber.renourisheddelight.data.LevelFoodConfig;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import dev.architectury.event.events.common.LifecycleEvent;
import org.slf4j.Logger;

public final class RenourishedDelightMod {

    public static final String MOD_ID = "renourisheddelight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        ClientConfiguration.init();
        CommonConfiguration.init();
        GameRuleRegistry.init();
        FoodConfigDataLoader.init();
        LifecycleEvent.SERVER_STOPPED.register(x -> {
            FoodPresetRegistry.init();
            LevelFoodConfig.init();
        });
    }

    public static void initClient() {
        FoodBarOverlay.init();
    }
}
