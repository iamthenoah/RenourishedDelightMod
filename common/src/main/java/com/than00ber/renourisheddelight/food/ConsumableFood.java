package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
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
        this(properties != null ? properties.nutrition() : 2, properties != null ? properties.saturation() : 0.0F);
    }

    public ConsumableFood(int nutrition, float saturation) {
        duration = toDuration(nutrition, saturation);
        hearts = toHearts(nutrition);
    }

    public ConsumableFoodInstance create(Item item) {
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        int duration = properties != null ? toDuration(properties.nutrition(), properties.saturation()) : THIRTY_SECONDS;
        int hearts = properties != null ? toHearts(properties.nutrition()) : ONE_HEART;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, String.valueOf(UUID.randomUUID()));
        AttributeModifier modifier = new AttributeModifier(id, hearts, AttributeModifier.Operation.ADD_VALUE);
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
