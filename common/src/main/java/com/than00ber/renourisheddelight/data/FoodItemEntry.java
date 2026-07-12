package com.than00ber.renourisheddelight.data;

import com.than00ber.renourisheddelight.food.AttributeBonus;

import java.util.List;
import java.util.stream.Collectors;

public final class FoodItemEntry {

    public String item;
    public List<AttributeBonus> attributes;
    public boolean override;

    public FoodItemEntry(String item, List<AttributeBonus> attributes) {
        this(item, attributes, false);
    }

    public FoodItemEntry(String item, List<AttributeBonus> attributes, boolean override) {
        this.item = item;
        this.attributes = attributes;
        this.override = override;
    }
    
    public FoodItemEntry copy() {
        List<AttributeBonus> copy = attributes.stream().map(AttributeBonus::copy).collect(Collectors.toList());
        return new FoodItemEntry(item, copy, override);
    }
}