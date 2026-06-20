package com.than00ber.renourisheddelight.registry;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.effect.FedMobEffect;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public final class EffectRegistry {

    public static Holder<MobEffect> FED;

    public static void init() {
        FED = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Registry.register(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "fed"),
                new FedMobEffect()));
    }
}