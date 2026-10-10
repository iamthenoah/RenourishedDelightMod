package com.than00ber.renourisheddelight.fabric;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public final class RenourishedDelightModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        RenourishedDelightMod.init();
        EntityDataSerializers.registerSerializer(Diet.DATA_SERIALIZER);
        FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(Items.BEEF), PotionRegistry.nourishment());
    }
}
