package com.than00ber.renourisheddelight.food;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class EffectBonus {

    public String effect;
    public int level;
    public int duration;

    @SuppressWarnings("unused")
    public EffectBonus() {
        // needed for persisted config
    }

    public EffectBonus(String effect, int level, int duration) {
        this.effect = effect;
        this.level = level;
        this.duration = duration;
    }

    public EffectBonus copy() {
        return new EffectBonus(effect, level, duration);
    }

    public boolean matches(EffectBonus other) {
        return level == other.level && duration == other.duration && Objects.equals(effect, other.effect);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Effect", effect != null ? effect : "");
        tag.putInt("Level", level);
        tag.putInt("Duration", duration);
        return tag;
    }

    public static EffectBonus load(CompoundTag tag) {
        return new EffectBonus(tag.getString("Effect"), tag.getInt("Level"), tag.getInt("Duration"));
    }

    public static @Nullable Holder<MobEffect> resolve(String id) {
        try {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(id));
            return effect != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect) : null;
        } catch (Exception exception) {
            return null;
        }
    }
}
