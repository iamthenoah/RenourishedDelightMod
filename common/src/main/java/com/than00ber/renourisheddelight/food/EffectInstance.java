package com.than00ber.renourisheddelight.food;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

public final class EffectInstance {

    private final Holder<MobEffect> effect;
    private final int level;
    private final int duration;
    private int time;

    public EffectInstance(Holder<MobEffect> effect, int level, int duration, int time) {
        this.effect = effect;
        this.level = level;
        this.duration = duration;
        this.time = time;
    }

    public Holder<MobEffect> effect() {
        return effect;
    }

    public int level() {
        return level;
    }

    public int duration() {
        return duration;
    }

    public int time() {
        return time;
    }

    public int remaining() {
        return Math.max(0, duration - time);
    }

    public boolean isExpired() {
        return time >= duration;
    }

    public void tick(int ticks) {
        time = Math.max(0, time + ticks);
    }

    public MobEffectInstance toMobEffect() {
        return new MobEffectInstance(effect, remaining(), Math.max(0, level - 1), false, true, true);
    }

    public static CompoundTag save(EffectInstance instance) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Effect", BuiltInRegistries.MOB_EFFECT.getKey(instance.effect.value()).toString());
        tag.putInt("Level", instance.level);
        tag.putInt("Duration", instance.duration);
        tag.putInt("Time", instance.time);
        return tag;
    }

    public static @Nullable EffectInstance load(CompoundTag tag) {
        Holder<MobEffect> effect = EffectBonus.resolve(tag.getString("Effect"));
        if (effect == null) return null;
        return new EffectInstance(effect, tag.getInt("Level"), tag.getInt("Duration"), tag.getInt("Time"));
    }
}
