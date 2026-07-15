package com.than00ber.renourisheddelight.data;

import com.than00ber.renourisheddelight.food.AttributeBonus;

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
}