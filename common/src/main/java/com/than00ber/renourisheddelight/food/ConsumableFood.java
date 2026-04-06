package com.than00ber.renourisheddelight.food;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.UUID;

public class ConsumableFood {

    private static final int ONE_HEART = 1;
    private static final int THIRTY_SECONDS = 20 * 30;

    public final int duration;
    public final int hearts;

    public ConsumableFood(FoodProperties properties) {
        duration = properties != null ? toDuration(properties.getNutrition(), properties.getSaturationModifier()) : THIRTY_SECONDS;
        hearts = properties != null ? toHearts(properties.getNutrition()) : ONE_HEART;
    }

    public ConsumableFoodInstance create(Item item) {
        FoodProperties properties = item.getFoodProperties();
        int duration = properties != null ? toDuration(properties.getNutrition(), properties.getSaturationModifier()) : THIRTY_SECONDS;
        int hearts = properties != null ? toHearts(properties.getNutrition()) : ONE_HEART;
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "food_hearts", hearts, AttributeModifier.Operation.ADDITION);
        return new ConsumableFoodInstance(item, modifier, duration, 0);
    }

    private static int toHearts(int nutrition) {
        return Math.toIntExact(Mth.clamp(nutrition / 2, 1, 10));
    }

    private static int toDuration(int nutrition, float saturation) {
        int base = THIRTY_SECONDS + (int) (THIRTY_SECONDS * (nutrition * saturation * 2.0F));
        return Math.round(base / (float) THIRTY_SECONDS) * THIRTY_SECONDS;
    }
}
