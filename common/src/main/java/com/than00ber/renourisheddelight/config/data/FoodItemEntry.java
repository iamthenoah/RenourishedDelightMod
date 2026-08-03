package com.than00ber.renourisheddelight.config.data;

import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FoodItemEntry {

    public String item;
    public List<AttributeBonus> attributes;
    public boolean override;

    @SuppressWarnings("unused")
    public FoodItemEntry() {
        // needed for persisted config
    }

    public FoodItemEntry(String item, List<AttributeBonus> attributes) {
        this(item, attributes, false);
    }

    public FoodItemEntry(String item, List<AttributeBonus> attributes, boolean override) {
        this.item = item;
        this.attributes = attributes;
        this.override = override;
    }

    public FoodItemEntry copy() {
        List<AttributeBonus> bonuses = new ArrayList<>();
        attributes.forEach(x -> bonuses.add(x.copy()));
        return new FoodItemEntry(item, bonuses, override);
    }

    public static @Nullable FoodItemEntry get(List<FoodItemEntry> entries, Item item) {
        return get(entries, BuiltInRegistries.ITEM.getKey(item).toString());
    }

    public static @Nullable FoodItemEntry get(List<FoodItemEntry> entries, String id) {
        return entries.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);
    }
}
