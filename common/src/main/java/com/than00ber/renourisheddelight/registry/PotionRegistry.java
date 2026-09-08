package com.than00ber.renourisheddelight.registry;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.effect.NourishmentMobEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.Potion;

public final class PotionRegistry {

    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(RenourishedDelightMod.MOD_ID, Registries.POTION);

    public static final RegistrySupplier<Potion> NOURISHMENT = POTIONS.register("nourishment", NourishmentMobEffect::createPotion);

    public static void init() {
        POTIONS.register();
    }

    public static Holder<Potion> nourishment() {
        return BuiltInRegistries.POTION.wrapAsHolder(NOURISHMENT.get());
    }
}
