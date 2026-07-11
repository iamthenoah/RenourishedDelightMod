package com.than00ber.renourisheddelight;

import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;

public final class RenourishedDelightMod {

    public static final String MOD_ID = "renourisheddelight";

    public static void init() {
        Configuration.init();
        GameRuleRegistry.init();
    }

    public static void initClient() {
        FoodBarOverlay.init();
    }
}
