package com.than00ber.renourisheddelight;

import com.mojang.logging.LogUtils;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.data.FoodConfigReloadListener;
import com.than00ber.renourisheddelight.network.FoodConfigSyncPayload;
import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class RenourishedDelightMod {

    public static final String MOD_ID = "renourisheddelight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        EffectRegistry.init();
        PotionRegistry.init();
        ClientConfiguration.init();
        CommonConfiguration.init();
        GameRuleRegistry.init();
        FoodConfigReloadListener.init();
        SuppressHurtFlashPayload.init();
        FoodConfigSyncPayload.init();
    }

    public static void initClient() {
        FoodBarOverlay.init();
        TextureAtlasResourceLoader.init();
    }
    
    public static ResourceLocation key(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
