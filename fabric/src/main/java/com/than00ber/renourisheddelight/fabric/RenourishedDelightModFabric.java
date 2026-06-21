package com.than00ber.renourisheddelight.fabric;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import com.than00ber.renourisheddelight.food.Diet;
import dev.architectury.registry.ReloadListenerRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.packs.PackType;

public final class RenourishedDelightModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EntityDataSerializers.registerSerializer(Diet.DATA_SERIALIZER);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, TextureAtlasResourceLoader.getInstance());
        RenourishedDelightMod.init();
    }
}
