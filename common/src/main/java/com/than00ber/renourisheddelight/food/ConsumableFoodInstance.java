package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.data.FoodConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public record ConsumableFoodInstance(Item item, List<AttributeModifierInstance> attributes, List<EffectInstance> effects) {

    public int duration() {
        return Math.max(
                attributes.stream().mapToInt(AttributeModifierInstance::duration).max().orElse(0),
                effects.stream().mapToInt(EffectInstance::duration).max().orElse(0));
    }

    public int time() {
        int duration = duration();
        return Stream.concat(
                        attributes.stream().filter(x -> x.duration() == duration).map(AttributeModifierInstance::time),
                        effects.stream().filter(x -> x.duration() == duration).map(EffectInstance::time))
                .findFirst()
                .orElse(0);
    }

    public boolean isExpired() {
        return attributes.isEmpty() && effects.isEmpty();
    }

    public void tick(int ticks) {
        attributes.forEach(x -> x.tick(ticks));
        effects.forEach(x -> x.tick(ticks));
    }

    public static ConsumableFoodInstance create(Item item, FoodConfig config) {
        return create(item, config, 0);
    }

    public static ConsumableFoodInstance create(Item item, FoodConfig config, int decay) {
        List<AttributeModifierInstance> attributes = new ArrayList<>();
        List<EffectInstance> effects = new ArrayList<>();
        int value = Math.max(0, Diet.FULL_NUTRITION - decay);

        for (AttributeBonus bonus : config.bonuses(item)) {
            AttributeModifierInstance instance = resolveBonus(bonus, config, value);
            if (instance != null) attributes.add(instance);
        }
        for (EffectBonus bonus : config.effects(item)) {
            EffectInstance instance = resolveEffect(bonus, value);
            if (instance != null) effects.add(instance);
        }
        return new ConsumableFoodInstance(item, attributes, effects);
    }

    private static @Nullable EffectInstance resolveEffect(EffectBonus bonus, int value) {
        Holder<MobEffect> effect = EffectBonus.resolve(bonus.effect);
        if (effect == null) return null;

        int duration = Math.max(1, (int) Math.round(bonus.duration * value / (double) Diet.FULL_NUTRITION));
        return new EffectInstance(effect, Math.max(1, bonus.level), duration, 0);
    }

    private static @Nullable AttributeModifierInstance resolveBonus(AttributeBonus bonus, FoodConfig config, int value) {
        Holder<Attribute> attribute = resolveAttribute(bonus.attribute);

        if (attribute != null) {
            ResourceLocation id = RenourishedDelightMod.key(String.valueOf(UUID.randomUUID()));
            AttributeModifier modifier = new AttributeModifier(id, bonus.amount, parseOperation(bonus.operation));
            int duration = Math.max(1, (int) Math.round(config.effectiveDuration(bonus) * value / (double) Diet.FULL_NUTRITION));
            return new AttributeModifierInstance(attribute, modifier, duration, 0);
        } else {
            return null;
        }
    }

    public static @Nullable Holder<Attribute> resolveAttribute(String id) {
        Attribute attribute = tryGetAttribute(id);

        if (attribute == null && id != null) {
            int colon = id.indexOf(':');
            String namespace = colon >= 0 ? id.substring(0, colon) : "minecraft";
            String path = colon >= 0 ? id.substring(colon + 1) : id;

            if (!path.contains(".")) {
                attribute = tryGetAttribute(namespace + ":generic." + path);
            }
        }
        return attribute != null ? BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute) : null;
    }

    private static @Nullable Attribute tryGetAttribute(String id) {
        try {
            return BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(id));
        } catch (Exception exception) {
            return null;
        }
    }

    private static AttributeModifier.Operation parseOperation(String value) {
        try {
            return AttributeModifier.Operation.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            return AttributeModifier.Operation.ADD_VALUE;
        }
    }

    public static CompoundTag save(ConsumableFoodInstance instance) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Item", BuiltInRegistries.ITEM.getKey(instance.item).toString());
        ListTag attributes = new ListTag();
        instance.attributes.forEach(x -> attributes.add(AttributeModifierInstance.save(x)));
        tag.put("Attributes", attributes);
        ListTag effects = new ListTag();
        instance.effects.forEach(x -> effects.add(EffectInstance.save(x)));
        tag.put("Effects", effects);
        return tag;
    }

    public static ConsumableFoodInstance load(CompoundTag compoundTag) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(compoundTag.getString("Item")));
        List<AttributeModifierInstance> attributes = new ArrayList<>();

        List<EffectInstance> effects = new ArrayList<>();

        for (Tag tag : compoundTag.getList("Attributes", Tag.TAG_COMPOUND)) {
            AttributeModifierInstance bonus = AttributeModifierInstance.load((CompoundTag) tag);
            if (bonus != null) attributes.add(bonus);
        }
        for (Tag tag : compoundTag.getList("Effects", Tag.TAG_COMPOUND)) {
            EffectInstance instance = EffectInstance.load((CompoundTag) tag);
            if (instance != null) effects.add(instance);
        }
        return new ConsumableFoodInstance(item, attributes, effects);
    }
}
