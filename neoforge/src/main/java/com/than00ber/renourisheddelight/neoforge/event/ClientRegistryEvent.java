package com.than00ber.renourisheddelight.neoforge.event;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.client.KeyBindings;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientRegistryEvent {

    @SubscribeEvent
    public static void onFMLClientSetupEvent(FMLClientSetupEvent event) {
        RenourishedDelightMod.initClient();
    }

    @SubscribeEvent
    public static void onRegisterKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.VIEW_NUTRITION);
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListenersEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(TextureAtlasResourceLoader.getInstance());
    }
}
