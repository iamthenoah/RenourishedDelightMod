package com.than00ber.renourisheddelight;

import com.mojang.logging.LogUtils;
import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import dev.architectury.event.events.common.LifecycleEvent;
import org.slf4j.Logger;

public final class RenourishedDelightMod {

    public static final String MOD_ID = "renourisheddelight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        Configuration.init();
        GameRuleRegistry.init();
        LifecycleEvent.SETUP.register(() -> Configuration.Common.getInstance().populateDefaults());
    }

    public static void initClient() {
        FoodBarOverlay.init();
    }
}
