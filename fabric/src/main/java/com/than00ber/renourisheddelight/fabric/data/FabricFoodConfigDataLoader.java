package com.than00ber.renourisheddelight.fabric.data;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.data.FoodConfigDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public final class FabricFoodConfigDataLoader extends FoodConfigDataLoader implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "presets");
    }
}
