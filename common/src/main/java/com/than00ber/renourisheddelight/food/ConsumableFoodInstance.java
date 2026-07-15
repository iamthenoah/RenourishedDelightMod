package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.WorldFoodConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record ConsumableFoodInstance(Item item, List<AttributeModifierInstance> attributes) {

    private static final int SIXTY_SECONDS = 20 * 60;

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

    public static ConsumableFoodInstance create(Item item, @Nullable FoodProperties properties) {
        return create(item, properties, CommonConfiguration.getInstance().getAttributes(item));
    }

    public static ConsumableFoodInstance create(Item item, @Nullable FoodProperties properties, MinecraftServer server) {
        return create(item, properties, WorldFoodConfig.get(server).getAttributes(item));
    }

    private static ConsumableFoodInstance create(Item item, @Nullable FoodProperties properties, List<AttributeBonus> bonuses) {
        int nutrition = properties != null ? properties.nutrition() : 2;
        float saturation = properties != null ? properties.saturation() : 0.0F;
        List<AttributeModifierInstance> attributes = new ArrayList<>();

        for (AttributeBonus bonus : bonuses) {
            AttributeModifierInstance instance = resolveBonus(bonus);
            if (instance != null) attributes.add(instance);
        }
        if (attributes.stream().noneMatch(x -> x.attribute().value() == Attributes.MAX_HEALTH.value())) {
            AttributeBonus maxHealth = new AttributeBonus(
                    Attributes.MAX_HEALTH.getRegisteredName(),
                    AttributeModifier.Operation.ADD_VALUE.getSerializedName(),
                    Math.max(1, toHearts(nutrition, saturation)),
                    toDuration(nutrition, saturation));
            AttributeModifierInstance health = resolveBonus(maxHealth);
            if (health != null) attributes.addFirst(health);
        }
        return new ConsumableFoodInstance(item, attributes);
    }

    private static @Nullable AttributeModifierInstance resolveBonus(AttributeBonus bonus) {
        Holder<Attribute> attribute = resolveAttribute(bonus.attribute);
        if (attribute == null) return null;
        AttributeModifier.Operation operation = parseOperation(bonus.operation);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, String.valueOf(UUID.randomUUID()));
        int duration = Math.max(1, bonus.duration);
        AttributeModifier modifier = new AttributeModifier(id, bonus.amount, operation);
        return new AttributeModifierInstance(attribute, modifier, duration, 0);
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

    public static int toHearts(int nutrition, float saturation) {
        float score = (nutrition - 4) * 0.4F + saturation * 0.6F;
        return (int) Math.max(0, Math.floor(score / 2));
    }

    public static int toDuration(int nutrition, float saturation) {
        double product = Math.max(0.0D, nutrition * (double) saturation);
        double seconds = product > 0 ? 135.0D * Math.pow(product, 0.527D) : 0.0D;
        long minutes = Math.round(seconds / 60.0D);
        return (int) Math.max(SIXTY_SECONDS, minutes * SIXTY_SECONDS);
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
