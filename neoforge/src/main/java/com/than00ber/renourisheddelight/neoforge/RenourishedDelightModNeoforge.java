package com.than00ber.renourisheddelight.neoforge;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.neoforge.event.RenderEvent;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(RenourishedDelightMod.MOD_ID)
public final class RenourishedDelightModNeoforge {

    public RenourishedDelightModNeoforge(IEventBus bus) {
        DeferredRegister<EntityDataSerializer<?>> serializers = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, RenourishedDelightMod.MOD_ID);
        serializers.register("diet", () -> Diet.DATA_SERIALIZER);
        serializers.register(bus);

        RenourishedDelightMod.init();
        NeoForge.EVENT_BUS.register(RenderEvent.class);
    }
}