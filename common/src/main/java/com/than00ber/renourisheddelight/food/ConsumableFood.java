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
    private static final int SIXTY_SECONDS = 20 * 60;

    public final int duration;
    public final int hearts;

    public ConsumableFood(FoodProperties properties) {
        this(properties != null ? properties.nutrition() : 2, properties != null ? properties.saturation() : 0.0F);
    }

    public ConsumableFood(int nutrition, float saturation) {
        duration = toDuration(nutrition, saturation);
        hearts = toHearts(nutrition, saturation);
    }

    public ConsumableFoodInstance create(Item item) {
        Configuration.Common common = Configuration.Common.getInstance();
        Configuration.Common.FoodItemConfiguration itemConfig = common.getItemConfig(item);

        int hearts = itemConfig != null ? itemConfig.hearts : ONE_HEART;
        int duration = itemConfig != null ? itemConfig.duration : SIXTY_SECONDS;
        hearts = Math.max(1, Math.round(hearts * (float) common.foodHeartsMultiplier));
        duration = Math.round(duration * (float) common.foodDurationMultiplier);

        String name = String.valueOf(UUID.randomUUID());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, name);
        AttributeModifier modifier = new AttributeModifier(id, hearts, AttributeModifier.Operation.ADD_VALUE);
        return new ConsumableFoodInstance(item, modifier, duration, 0);
    }

    public static int toHearts(int nutrition, float saturation) {
        float score = (nutrition - 4) * 0.4F + saturation * 0.6F;
        return (int) Math.max(0, Math.floor(score / 2));
    }

    // Tuned so that low-nutrition*saturation foods (e.g. a Sweet Berry Cookie, product ~0.8) land
    // around 2 minutes, and the highest-tier feast foods (e.g. Shepherd's Pie, product ~294) land
    // around 45 minutes. A sub-linear power curve is used instead of a linear one because the
    // nutrition*saturation product spans a much wider range (~0.2 to ~294) than we want the
    // resulting duration to (a couple minutes to under an hour) - a linear mapping made cheap foods
    // last only ~1 minute while the best foods stretched out past 2 hours. The result is always
    // rounded to a whole minute, with a 1-minute floor.
    private static final double DURATION_SCALE_SECONDS = 135.0D;
    private static final double DURATION_EXPONENT = 0.527D;
    private static final int ONE_MINUTE = 20 * 60;

    public static int toDuration(int nutrition, float saturation) {
        double product = Math.max(0.0D, nutrition * (double) saturation);
        double seconds = product > 0 ? DURATION_SCALE_SECONDS * Math.pow(product, DURATION_EXPONENT) : 0.0D;
        long minutes = Math.round(seconds / 60.0D);
        return (int) Math.max(ONE_MINUTE, minutes * ONE_MINUTE);
    }
}
