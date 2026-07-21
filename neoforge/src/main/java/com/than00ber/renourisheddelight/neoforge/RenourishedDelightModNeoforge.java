package com.than00ber.renourisheddelight.neoforge;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.effect.NourishmentMobEffect;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(RenourishedDelightMod.MOD_ID)
public final class RenourishedDelightModNeoforge {

    public RenourishedDelightModNeoforge(IEventBus bus) {
        RenourishedDelightMod.init();
        
        DeferredRegister<EntityDataSerializer<?>> serializers = DeferredRegister.create(
                NeoForgeRegistries.ENTITY_DATA_SERIALIZERS,
                RenourishedDelightMod.MOD_ID);
        DeferredRegister<MobEffect> effects = DeferredRegister.create(
                Registries.MOB_EFFECT,
                RenourishedDelightMod.MOD_ID);
        DeferredRegister<Potion> potions = DeferredRegister.create(
                Registries.POTION,
                RenourishedDelightMod.MOD_ID);

        serializers.register("diet", () -> Diet.DATA_SERIALIZER);
        EffectRegistry.NOURISHMENT = effects.register("nourishment", NourishmentMobEffect::new);
        PotionRegistry.NOURISHMENT = potions.register("nourishment", NourishmentMobEffect::createPotion);

        serializers.register(bus);
        effects.register(bus);
        potions.register(bus);
    }
}