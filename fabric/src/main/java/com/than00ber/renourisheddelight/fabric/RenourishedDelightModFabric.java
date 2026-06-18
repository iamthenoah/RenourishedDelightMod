package com.than00ber.renourisheddelight.fabric;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.food.Diet;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public final class RenourishedDelightModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        RenourishedDelightMod.init();
        EntityDataSerializers.registerSerializer(Diet.DATA_SERIALIZER);
    }
}
