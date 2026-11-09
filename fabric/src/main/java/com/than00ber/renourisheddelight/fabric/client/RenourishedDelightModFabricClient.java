package com.than00ber.renourisheddelight.fabric.client;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.client.KeyBindings;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.server.packs.PackType;

public final class RenourishedDelightModFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, TextureAtlasResourceLoader.getInstance());
        KeyMappingRegistry.register(KeyBindings.VIEW_NUTRITION);
        RenourishedDelightMod.initClient();
    }
}
