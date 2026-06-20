package com.than00ber.renourisheddelight.neoforge;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.effect.FedMobEffect;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(RenourishedDelightMod.MOD_ID)
public final class RenourishedDelightModNeoforge {

    public RenourishedDelightModNeoforge(IEventBus bus) {
        DeferredRegister<EntityDataSerializer<?>> serializers = DeferredRegister.create(
                NeoForgeRegistries.ENTITY_DATA_SERIALIZERS,
                RenourishedDelightMod.MOD_ID);
        serializers.register("diet", () -> Diet.DATA_SERIALIZER);
        serializers.register(bus);
        DeferredRegister<MobEffect> effects = DeferredRegister.create(
                Registries.MOB_EFFECT,
                RenourishedDelightMod.MOD_ID);
        EffectRegistry.FED = effects.register("fed", FedMobEffect::new);
        effects.register(bus);
        RenourishedDelightMod.init();
    }
}