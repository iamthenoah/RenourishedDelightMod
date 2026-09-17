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

public final class FoodItemEntry {

    public String item;
    public List<AttributeBonus> attributes;

    @SuppressWarnings("unused")
    public FoodItemEntry() {
    }

    public FoodItemEntry(String item, List<AttributeBonus> attributes) {
        this.item = item;
        this.attributes = attributes;
    }

    public FoodItemEntry copy() {
        List<AttributeBonus> bonuses = new ArrayList<>();
        attributes.forEach(x -> bonuses.add(x.copy()));
        return new FoodItemEntry(item, bonuses);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Item", item != null ? item : "");
        ListTag list = new ListTag();
        attributes.forEach(x -> list.add(x.save()));
        tag.put("Attributes", list);
        return tag;
    }

    public static FoodItemEntry load(CompoundTag tag) {
        List<AttributeBonus> bonuses = new ArrayList<>();
        for (Tag bonus : tag.getList("Attributes", Tag.TAG_COMPOUND)) {
            bonuses.add(AttributeBonus.load((CompoundTag) bonus));
        }
        return new FoodItemEntry(tag.getString("Item"), bonuses);
    }

    public static @Nullable FoodItemEntry get(List<FoodItemEntry> entries, Item item) {
        return get(entries, BuiltInRegistries.ITEM.getKey(item).toString());
    }

    public static @Nullable FoodItemEntry get(List<FoodItemEntry> entries, String id) {
        return entries.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);
    }
}
