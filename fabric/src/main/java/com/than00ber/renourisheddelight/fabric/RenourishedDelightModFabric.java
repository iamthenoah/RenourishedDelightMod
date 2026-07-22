package com.than00ber.renourisheddelight.fabric;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.effect.NourishmentMobEffect;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

public final class RenourishedDelightModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        RenourishedDelightMod.init();
        
        EntityDataSerializers.registerSerializer(Diet.DATA_SERIALIZER);
        EffectRegistry.NOURISHMENT = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Registry.register(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "nourishment"),
                new NourishmentMobEffect()));
        PotionRegistry.NOURISHMENT = BuiltInRegistries.POTION.wrapAsHolder(Registry.register(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "nourishment"),
                NourishmentMobEffect.createPotion()));
        FabricBrewingRecipeRegistryBuilder.BUILD.register(x -> x.addMix(Potions.AWKWARD, Items.BEEF, PotionRegistry.NOURISHMENT));
    }
}
