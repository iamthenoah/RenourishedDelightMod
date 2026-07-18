package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.config.data.FoodPresetRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class AttributeBonus {

    public String attribute;
    public String operation;
    public double amount;
    public int duration;

    @SuppressWarnings("unused")
    public AttributeBonus() {
        // needed for persisted config
    }

    public AttributeBonus(String attribute, String operation, double amount, int duration) {
        this.attribute = attribute;
        this.operation = operation;
        this.amount = amount;
        this.duration = duration;
    }
    
    public AttributeBonus copy() {
        return new AttributeBonus(attribute, operation, amount, duration);
    }

    public static List<AttributeBonus> computeDefaultBonuses(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        FoodItemEntry preset = FoodPresetRegistry.getInstance().get(id);
        boolean overridden = preset != null && preset.override && !preset.attributes.isEmpty();
        List<AttributeBonus> bonuses = new ArrayList<>();

        if (!overridden) {
            bonuses.add(computeGenericDefault(item));
        }
        if (preset != null) {
            for (AttributeBonus bonus : preset.attributes) {
                bonuses.add(bonus.copy());
            }
        }
        return bonuses;
    }

    public static AttributeBonus computeGenericDefault(Item item) {
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        int nutrition = properties != null ? properties.nutrition() : 2;
        float saturation = properties != null ? properties.saturation() : 0.0F;

        return new AttributeBonus(
                Attributes.MAX_HEALTH.getRegisteredName(),
                AttributeModifier.Operation.ADD_VALUE.getSerializedName(),
                Math.max(1, ConsumableFoodInstance.toHearts(nutrition, saturation)),
                ConsumableFoodInstance.toDuration(nutrition, saturation));
    }
}