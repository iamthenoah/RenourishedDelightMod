package com.than00ber.renourisheddelight;

import com.mojang.logging.LogUtils;
import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfigDataLoader;
import com.than00ber.renourisheddelight.config.data.FoodPresetRegistry;
import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import org.slf4j.Logger;

public final class RenourishedDelightMod {

    public static final String MOD_ID = "renourisheddelight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        FoodPresetRegistry.init();
        ClientConfiguration.init();
        CommonConfiguration.init();
        GameRuleRegistry.init();
        FoodConfigDataLoader.init();
    }

    public static void initClient() {
        FoodBarOverlay.init();
        SuppressHurtFlashPayload.init();
    }
}
