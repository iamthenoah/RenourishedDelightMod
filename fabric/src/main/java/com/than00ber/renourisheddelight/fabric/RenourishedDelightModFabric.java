package com.than00ber.renourisheddelight.fabric;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import com.than00ber.renourisheddelight.effect.NourishmentMobEffect;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import dev.architectury.registry.ReloadListenerRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public final class RenourishedDelightModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EntityDataSerializers.registerSerializer(Diet.DATA_SERIALIZER);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, TextureAtlasResourceLoader.getInstance());
        EffectRegistry.NOURISHMENT = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Registry.register(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "nourishment"),
                new NourishmentMobEffect()));
        RenourishedDelightMod.init();
    }
}
