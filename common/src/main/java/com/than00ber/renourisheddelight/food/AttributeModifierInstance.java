package com.than00ber.renourisheddelight.food;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
        Tag modifierTag = AttributeModifier.CODEC.encodeStart(NbtOps.INSTANCE, instance.modifier).getOrThrow();
        compoundTag.put("Modifier", modifierTag);
        compoundTag.putInt("Duration", instance.duration);
        compoundTag.putInt("Time", instance.time);
        return compoundTag;
    }

    public static @Nullable AttributeModifierInstance load(CompoundTag compoundTag) {
        Optional<String> attributeId = compoundTag.getString("Attribute");
        if (attributeId.isEmpty()) return null;
        Optional<Holder.Reference<Attribute>> attribute = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attributeId.get()));
        if (attribute.isEmpty()) return null;
        Optional<CompoundTag> modifierTag = compoundTag.getCompound("Modifier");
        if (modifierTag.isEmpty()) return null;
        Optional<AttributeModifier> modifier = AttributeModifier.CODEC.parse(NbtOps.INSTANCE, modifierTag.get()).result();
        if (modifier.isEmpty()) return null;
        int duration = compoundTag.getInt("Duration").orElse(0);
        int time = compoundTag.getInt("Time").orElse(0);
        return new AttributeModifierInstance(attribute.get(), modifier.get(), duration, time);
    }
}
