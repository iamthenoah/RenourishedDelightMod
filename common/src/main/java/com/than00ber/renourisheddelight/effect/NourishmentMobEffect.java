package com.than00ber.renourisheddelight.effect;

import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public final class NourishmentMobEffect extends MobEffect {

    public static Potion createPotion() {
        return new Potion(new MobEffectInstance(EffectRegistry.NOURISHMENT, 9600, 0));
    }

    public NourishmentMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8A33D);
    }
}