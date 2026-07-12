package com.than00ber.renourisheddelight.data;

import com.than00ber.renourisheddelight.food.AttributeBonus;

import java.util.ArrayList;
import java.util.List;

public final class FoodItemEntry {

    public String item = "";
    public boolean override = false;
    public List<AttributeBonus> attributes = new ArrayList<>();
}