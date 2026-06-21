package com.than00ber.renourisheddelight.neoforge.event;

import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class RegistryEvent {

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(TextureAtlasResourceLoader.getInstance());
    }
}