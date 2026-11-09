package com.than00ber.renourisheddelight.forge.event;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.client.KeyBindings;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RenourishedDelightMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientRegistryEvent {

    @SubscribeEvent
    public static void onFMLClientSetupEvent(FMLClientSetupEvent event) {
        RenourishedDelightMod.initClient();
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListenersEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(TextureAtlasResourceLoader.getInstance());
    }

    @SubscribeEvent
    public static void onRegisterKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.VIEW_NUTRITION);
    }
}
