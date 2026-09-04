package com.than00ber.renourisheddelight.config.data;

import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class FoodConfig {

    public List<FoodItemEntry> foods = new ArrayList<>();
    public List<DurationMultiplierEntry> multipliers = new ArrayList<>();
    public List<StarvationEntry> starvation = StarvationEntry.defaults();

    public FoodConfig() {
        // do nothing
    }

    public FoodConfig(FoodConfig other) {
        copyFrom(other.foods, other.multipliers, other.starvation);
    }

    public FoodConfig(List<FoodItemEntry> foods, List<DurationMultiplierEntry> multipliers, List<StarvationEntry> starvation) {
        copyFrom(foods, multipliers, starvation);
    }

    public void copyFrom(FoodConfig other) {
        copyFrom(other.foods, other.multipliers, other.starvation);
    }

    private void copyFrom(List<FoodItemEntry> foods, List<DurationMultiplierEntry> multipliers, List<StarvationEntry> starvation) {
        this.foods = new ArrayList<>();
        this.multipliers = new ArrayList<>();
        this.starvation = new ArrayList<>();
        foods.forEach(x -> this.foods.add(x.copy()));
        multipliers.forEach(x -> this.multipliers.add(x.copy()));
        starvation.forEach(x -> this.starvation.add(x.copy()));
    }

    public @Nullable FoodItemEntry entry(Item item) {
        return FoodItemEntry.get(foods, item);
    }

    public List<AttributeBonus> bonuses(Item item) {
        FoodItemEntry entry = entry(item);
        return entry != null ? entry.attributes : AttributeBonus.defaults(item);
    }

    public FoodItemEntry claim(Item item) {
        FoodItemEntry entry = entry(item);

        if (entry == null) {
            List<AttributeBonus> bonuses = new ArrayList<>();
            AttributeBonus.defaults(item).forEach(x -> bonuses.add(x.copy()));
            entry = new FoodItemEntry(BuiltInRegistries.ITEM.getKey(item).toString(), bonuses);
            foods.add(entry);
        }
        return entry;
    }

    public void reset(Item item) {
        FoodItemEntry entry = entry(item);
        if (entry != null) foods.remove(entry);
    }
    
    public void prune(Item item) {
        FoodItemEntry entry = entry(item);
        if (entry != null && matchesDefaults(entry, item)) foods.remove(entry);
    }

    private static boolean matchesDefaults(FoodItemEntry entry, Item item) {
        List<AttributeBonus> defaults = AttributeBonus.defaults(item);
        if (entry.attributes.size() != defaults.size()) return false;

        for (int i = 0; i < defaults.size(); i++) {
            AttributeBonus bonus = entry.attributes.get(i);
            AttributeBonus base = defaults.get(i);

            if (!bonus.sameAttribute(base) || bonus.amount != base.amount || bonus.duration != base.duration || !Objects.equals(bonus.operation, base.operation)) {
                return false;
            }
        }
        return true;
    }

    public double multiplier(String attribute) {
        DurationMultiplierEntry entry = DurationMultiplierEntry.get(multipliers, attribute);
        return entry != null ? entry.multiplier : 1.0;
    }

    public void setMultiplier(String attribute, double value) {
        DurationMultiplierEntry entry = DurationMultiplierEntry.get(multipliers, attribute);

        if (value == 1.0) {
            if (entry != null) multipliers.remove(entry);
        } else if (entry != null) {
            entry.multiplier = value;
        } else {
            multipliers.add(new DurationMultiplierEntry(attribute, value));
        }
    }

    public int effectiveDuration(AttributeBonus bonus) {
        return Math.max(1, (int) Math.round(bonus.duration * multiplier(bonus.attribute)));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.put("Entries", saveAll(foods, FoodItemEntry::save));
        tag.put("Multipliers", saveAll(multipliers, DurationMultiplierEntry::save));
        tag.put("Starvation", saveAll(starvation, StarvationEntry::save));
        return tag;
    }

    public static FoodConfig load(CompoundTag tag) {
        FoodConfig config = new FoodConfig();
        config.foods = loadAll(tag, "Entries", FoodItemEntry::load);
        config.multipliers = loadAll(tag, "Multipliers", DurationMultiplierEntry::load);
        config.starvation = loadAll(tag, "Starvation", StarvationEntry::load);
        return config;
    }

    private static <T> ListTag saveAll(List<T> entries, Function<T, CompoundTag> serializer) {
        ListTag list = new ListTag();
        entries.forEach(x -> list.add(serializer.apply(x)));
        return list;
    }

    private static <T> List<T> loadAll(CompoundTag tag, String key, Function<CompoundTag, T> deserializer) {
        List<T> entries = new ArrayList<>();

        for (Tag entry : tag.getList(key, Tag.TAG_COMPOUND)) {
            entries.add(deserializer.apply((CompoundTag) entry));
        }
        return entries;
    }
}
