package com.than00ber.renourisheddelight;

import com.mojang.logging.LogUtils;
import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.data.FoodConfigDataLoader;
import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public final class RenourishedDelightMod {

    public static final String MOD_ID = "renourisheddelight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        Configuration.init();
        GameRuleRegistry.init();
        LifecycleEvent.SETUP.register(() -> Configuration.Common.getInstance().populateDefaults());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new FoodConfigDataLoader(), FoodConfigDataLoader.ID);
    }

    public static void initClient() {
        FoodBarOverlay.init();
        SuppressHurtFlashPayload.init();
    }
}
