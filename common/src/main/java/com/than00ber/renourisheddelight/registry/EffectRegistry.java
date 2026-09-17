package com.than00ber.renourisheddelight.registry;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.effect.NourishmentMobEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public final class EffectRegistry {

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(RenourishedDelightMod.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> NOURISHMENT = EFFECTS.register("nourishment", NourishmentMobEffect::new);

    public static void init() {
        EFFECTS.register();
    }

    public static Holder<MobEffect> nourishment() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(NOURISHMENT.get());
    }
}
