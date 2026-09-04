package com.than00ber.renourisheddelight.food;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class AttributeBonus {

    private static final int ONE_MINUTE = 20 * 60;

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

    public boolean sameAttribute(AttributeBonus other) {
        Holder<Attribute> self = ConsumableFoodInstance.resolveAttribute(attribute);
        Holder<Attribute> that = ConsumableFoodInstance.resolveAttribute(other.attribute);
        if (self != null && that != null) return self.value() == that.value();
        return attribute != null && attribute.equals(other.attribute);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Attribute", attribute != null ? attribute : "");
        tag.putString("Operation", operation != null ? operation : "");
        tag.putDouble("Amount", amount);
        tag.putInt("Duration", duration);
        return tag;
    }

    public static AttributeBonus load(CompoundTag tag) {
        return new AttributeBonus(tag.getString("Attribute"), tag.getString("Operation"), tag.getDouble("Amount"), tag.getInt("Duration"));
    }

    public static List<AttributeBonus> defaults(Item item) {
        List<AttributeBonus> bonuses = new ArrayList<>();
        bonuses.add(defaultMaxHealth(item));
        return bonuses;
    }

    private static AttributeBonus defaultMaxHealth(Item item) {
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        int nutrition = properties != null ? properties.nutrition() : 2;
        float saturation = properties != null ? properties.saturation() : 0.0F;
        return new AttributeBonus(
                Attributes.MAX_HEALTH.getRegisteredName(),
                AttributeModifier.Operation.ADD_VALUE.getSerializedName(),
                Math.max(1, toHearts(nutrition, saturation)),
                toDuration(nutrition, saturation));
    }

    private static int toHearts(int nutrition, float saturation) {
        float score = (nutrition - 4) * 0.4F + saturation * 0.6F;
        return (int) Math.max(0, Math.floor(score / 2));
    }

    private static int toDuration(int nutrition, float saturation) {
        double product = Math.max(0.0D, nutrition * (double) saturation);
        double seconds = product > 0 ? 135.0D * Math.pow(product, 0.527D) : 0.0D;
        long minutes = Math.round(seconds / 60.0D);
        return (int) Math.max(ONE_MINUTE, minutes * ONE_MINUTE);
    }
}
