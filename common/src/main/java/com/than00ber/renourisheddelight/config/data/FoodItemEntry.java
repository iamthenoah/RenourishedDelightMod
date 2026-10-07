package com.than00ber.renourisheddelight.config.data;

import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.food.EffectBonus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FoodItemEntry {

    public String item;
    public List<AttributeBonus> attributes;
    public List<EffectBonus> effects = new ArrayList<>();

    @SuppressWarnings("unused")
    public FoodItemEntry() {
    }

    public FoodItemEntry(String item, List<AttributeBonus> attributes) {
        this(item, attributes, new ArrayList<>());
    }

    public FoodItemEntry(String item, List<AttributeBonus> attributes, List<EffectBonus> effects) {
        this.item = item;
        this.attributes = attributes;
        this.effects = effects;
    }

    public FoodItemEntry copy() {
        List<AttributeBonus> bonuses = new ArrayList<>();
        List<EffectBonus> granted = new ArrayList<>();
        attributes.forEach(x -> bonuses.add(x.copy()));
        effects.forEach(x -> granted.add(x.copy()));
        return new FoodItemEntry(item, bonuses, granted);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Item", item != null ? item : "");
        ListTag list = new ListTag();
        attributes.forEach(x -> list.add(x.save()));
        tag.put("Attributes", list);
        ListTag granted = new ListTag();
        effects.forEach(x -> granted.add(x.save()));
        tag.put("Effects", granted);
        return tag;
    }

    public static FoodItemEntry load(CompoundTag tag) {
        List<AttributeBonus> bonuses = new ArrayList<>();
        List<EffectBonus> granted = new ArrayList<>();

        for (Tag bonus : tag.getList("Attributes", Tag.TAG_COMPOUND)) {
            bonuses.add(AttributeBonus.load((CompoundTag) bonus));
        }
        for (Tag bonus : tag.getList("Effects", Tag.TAG_COMPOUND)) {
            granted.add(EffectBonus.load((CompoundTag) bonus));
        }
        return new FoodItemEntry(tag.getString("Item"), bonuses, granted);
    }

    public static @Nullable FoodItemEntry get(List<FoodItemEntry> entries, Item item) {
        return get(entries, BuiltInRegistries.ITEM.getKey(item).toString());
    }

    public static @Nullable FoodItemEntry get(List<FoodItemEntry> entries, String id) {
        return entries.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);
    }

    public static List<AttributeBonus> baseline(List<FoodItemEntry> presets, Item item) {
        FoodItemEntry preset = get(presets, item);
        List<AttributeBonus> bonuses = new ArrayList<>();
        (preset != null ? preset.attributes : AttributeBonus.defaults(item)).forEach(x -> bonuses.add(x.copy()));
        return bonuses;
    }

    public static List<EffectBonus> baselineEffects(List<FoodItemEntry> presets, Item item) {
        FoodItemEntry preset = get(presets, item);
        List<EffectBonus> granted = new ArrayList<>();
        if (preset != null) preset.effects.forEach(x -> granted.add(x.copy()));
        return granted;
    }
}
