package com.than00ber.renourisheddelight.config.data;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StarvationEntry {

    public String effect;
    public int after;
    public int amplifier;
    public int max;

    @SuppressWarnings("unused")
    public StarvationEntry() {
        // needed for persisted config
    }

    public StarvationEntry(String effect, int after, int amplifier, int max) {
        this.effect = effect;
        this.after = after;
        this.amplifier = amplifier;
        this.max = max;
    }

    public StarvationEntry copy() {
        return new StarvationEntry(effect, after, amplifier, max);
    }

    public int levelAt(int stagesSince) {
        return Math.clamp(Math.max(1, amplifier) + stagesSince, 1, Math.max(Math.max(1, amplifier), max));
    }

    public static List<StarvationEntry> defaults() {
        List<StarvationEntry> entries = new ArrayList<>();
        entries.add(new StarvationEntry("minecraft:slowness", 3600, 1, 3));
        entries.add(new StarvationEntry("minecraft:mining_fatigue", 7200, 1, 3));
        entries.add(new StarvationEntry("minecraft:weakness", 10800, 1, 2));
        return entries;
    }

    public static @Nullable Holder<MobEffect> resolveEffect(String id) {
        try {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(id));
            return effect != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect) : null;
        } catch (Exception exception) {
            return null;
        }
    }

    public static List<StarvationEntry> reached(List<StarvationEntry> entries, int ticks) {
        List<StarvationEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingInt(x -> x.after));
        sorted.removeIf(x -> x.after > ticks || x.after < 0);
        return sorted;
    }
}
