package com.than00ber.renourisheddelight.effect;

import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;

import java.util.ArrayList;
import java.util.List;

public final class NourishmentMobEffect extends MobEffect {

    public static Potion createPotion() {
        return new Potion(new MobEffectInstance(EffectRegistry.nourishment(), 9600, 0));
    }

    public NourishmentMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8A33D);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        cleanse(entity);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        cleanse(entity);
        return true;
    }

    private static void cleanse(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        List<Holder<MobEffect>> harmful = new ArrayList<>();

        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                harmful.add(instance.getEffect());
            }
        }
        harmful.forEach(entity::removeEffect);
    }
}
