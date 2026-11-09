package com.than00ber.renourisheddelight.registry;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.effect.NourishmentMobEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public final class EffectRegistry {

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(RenourishedDelightMod.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> NOURISHMENT = EFFECTS.register("nourishment", NourishmentMobEffect::new);

    private static final ResourceLocation FARMERS_DELIGHT_NOURISHMENT = new ResourceLocation("farmersdelight", "nourishment");

    public static void init() {
        EFFECTS.register();
    }

    public static MobEffect nourishment() {
        return NOURISHMENT.get();
    }

    public static boolean isNourished(LivingEntity entity) {
        return entity.hasEffect(nourishment()) || BuiltInRegistries.MOB_EFFECT.getOptional(FARMERS_DELIGHT_NOURISHMENT).map(entity::hasEffect).orElse(false);
    }
}
