package com.than00ber.renourisheddelight.food;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

public final class AttributeModifierInstance {

    private final Holder<Attribute> attribute;
    private final AttributeModifier modifier;
    private final int duration;
    private int time;

    public AttributeModifierInstance(Holder<Attribute> attribute, AttributeModifier modifier, int duration, int time) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.duration = duration;
        this.time = time;
    }

    public Holder<Attribute> attribute() {
        return attribute;
    }

    public AttributeModifier modifier() {
        return modifier;
    }

    public int duration() {
        return duration;
    }

    public int time() {
        return time;
    }

    public boolean isExpired() {
        return time >= duration;
    }

    public void tick(int ticks) {
        time = Math.max(0, time + ticks);
    }

    public static CompoundTag save(AttributeModifierInstance instance) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Attribute", BuiltInRegistries.ATTRIBUTE.getKey(instance.attribute.value()).toString());
        compoundTag.put("Modifier", instance.modifier.save());
        compoundTag.putInt("Duration", instance.duration);
        compoundTag.putInt("Time", instance.time);
        return compoundTag;
    }

    public static @Nullable AttributeModifierInstance load(CompoundTag compoundTag) {
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(compoundTag.getString("Attribute")));
        if (attribute == null) return null;
        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
        AttributeModifier modifier = AttributeModifier.load(compoundTag.getCompound("Modifier"));
        int duration = compoundTag.getInt("Duration");
        int time = compoundTag.getInt("Time");
        return new AttributeModifierInstance(holder, modifier, duration, time);
    }
}
