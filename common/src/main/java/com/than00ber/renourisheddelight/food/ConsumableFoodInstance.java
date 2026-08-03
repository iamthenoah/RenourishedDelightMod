package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.data.DurationMultiplierEntry;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record ConsumableFoodInstance(Item item, List<AttributeModifierInstance> attributes) {

    public int duration() {
        return attributes.stream().mapToInt(AttributeModifierInstance::duration)
                .max()
                .orElse(0);
    }

    public int time() {
        return attributes.stream()
                .max(Comparator.comparingInt(AttributeModifierInstance::duration))
                .map(AttributeModifierInstance::time)
                .orElse(0);
    }

    public boolean isExpired() {
        return attributes.isEmpty();
    }

    public void tick(int ticks) {
        attributes.forEach(x -> x.tick(ticks));
    }

    public ConsumableFoodInstance copy() {
        return new ConsumableFoodInstance(item, new ArrayList<>(attributes));
    }

    public static ConsumableFoodInstance create(Item item, FoodConfigHolder config) {
        FoodItemEntry entry = FoodItemEntry.get(config.getFoodConfig(), item);
        List<DurationMultiplierEntry> multipliers = config.getMultiplierConfig();
        List<AttributeModifierInstance> attributes = new ArrayList<>();

        if (entry != null) {
            for (AttributeBonus bonus : entry.attributes) {
                AttributeModifierInstance instance = resolveBonus(bonus, multipliers);
                if (instance != null) attributes.add(instance);
            }
        }
        boolean overridden = entry != null && entry.override;

        if (!overridden && attributes.stream().noneMatch(x -> x.attribute().value() == Attributes.MAX_HEALTH.value())) {
            AttributeModifierInstance health = resolveBonus(AttributeBonus.defaultMaxHealth(item), multipliers);

            if (health != null) {
                attributes.addFirst(health);
            }
        }
        return new ConsumableFoodInstance(item, attributes);
    }

    private static @Nullable AttributeModifierInstance resolveBonus(AttributeBonus bonus, List<DurationMultiplierEntry> multipliers) {
        Holder<Attribute> attribute = resolveAttribute(bonus.attribute);
        if (attribute == null) return null;
        AttributeModifier.Operation operation = parseOperation(bonus.operation);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, String.valueOf(UUID.randomUUID()));
        AttributeModifier modifier = new AttributeModifier(id, bonus.amount, operation);
        return new AttributeModifierInstance(attribute, modifier, bonus.effectiveDuration(multipliers), 0);
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
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Item", BuiltInRegistries.ITEM.getKey(instance.item).toString());
        ListTag attributes = new ListTag();
        instance.attributes.forEach(x -> attributes.add(AttributeModifierInstance.save(x)));
        compoundTag.put("Attributes", attributes);
        return compoundTag;
    }

    public static ConsumableFoodInstance load(CompoundTag compoundTag) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(compoundTag.getString("Item")));
        List<AttributeModifierInstance> attributes = new ArrayList<>();

        for (Tag tag : compoundTag.getList("Attributes", Tag.TAG_COMPOUND)) {
            AttributeModifierInstance bonus = AttributeModifierInstance.load((CompoundTag) tag);
            if (bonus != null) attributes.add(bonus);
        }
        return new ConsumableFoodInstance(item, attributes);
    }
}
