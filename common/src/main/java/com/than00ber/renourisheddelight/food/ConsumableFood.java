package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.resources.ResourceLocation;
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
        Configuration.Common common = Configuration.Common.getInstance();
        Configuration.Common.FoodItemConfiguration itemConfig = common.getItemConfig(item);

        int hearts = itemConfig != null ? itemConfig.hearts : ONE_HEART;
        int duration = itemConfig != null ? itemConfig.duration : THIRTY_SECONDS;
        hearts = Math.max(1, Math.round(hearts * (float) common.foodHeartsMultiplier));
        duration = Math.round(duration * (float) common.foodDurationMultiplier);

        String name = String.valueOf(UUID.randomUUID());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, name);
        AttributeModifier modifier = new AttributeModifier(id, hearts, AttributeModifier.Operation.ADD_VALUE);
        return new ConsumableFoodInstance(item, modifier, duration, 0);
    }

    public static int toHearts(int nutrition) {
        return Math.max(nutrition / 2 - ONE_HEART, 0);
    }

    public static int toDuration(int nutrition, float saturation) {
        int base = THIRTY_SECONDS + (int) (THIRTY_SECONDS * (nutrition * saturation));
        return Math.round(base / (float) THIRTY_SECONDS) * THIRTY_SECONDS;
    }
}
