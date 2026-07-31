package com.than00ber.renourisheddelight.fabric;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

public final class RenourishedDelightModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        RenourishedDelightMod.init();
        EntityDataSerializers.registerSerializer(Diet.DATA_SERIALIZER);
        FabricBrewingRecipeRegistryBuilder.BUILD.register(x -> x.addMix(Potions.AWKWARD, Items.BEEF, PotionRegistry.NOURISHMENT));
    }
}
