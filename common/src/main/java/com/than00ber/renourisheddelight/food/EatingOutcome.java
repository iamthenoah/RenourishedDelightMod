package com.than00ber.renourisheddelight.food;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

public enum EatingOutcome {
    CONSUME(true, null),
    EFFECTS_ONLY(true, null),
    REPLENISH(true, null),
    REPLACE_LOW(true, null),
    TOO_MANY(false, "message.eating_too_many"),
    NOT_BALANCED(false, "message.eating_not_balanced");

    final boolean success;
    final @Nullable String message;

    EatingOutcome(boolean success, @Nullable String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<MutableComponent> message() {
        return message != null ? Optional.of(Component.translatable(message)) : Optional.empty();
    }

    public void consume(ServerPlayer player, Diet diet, Item item) {
        switch (this) {
            case CONSUME -> {
                ConsumableFood food = new ConsumableFood(item.getFoodProperties());
                diet.addToSlot(player, food.create(item));
            }
            case EFFECTS_ONLY -> {
                FoodProperties properties = item.getFoodProperties();

                if (properties != null) {
                    properties.getEffects().forEach(x -> player.addEffect(new MobEffectInstance(x.getFirst())));
                }
            }
            case REPLENISH -> {
                ConsumableFoodInstance instance = diet.getSlots().stream()
                        .filter(x -> x.item == item)
                        .findFirst()
                        .orElse(null);

                if (instance != null) {
                    ConsumableFood food = new ConsumableFood(item.getFoodProperties());
                    instance.time -= Math.max(0, food.create(item).duration);
                }
            }
            case REPLACE_LOW -> {
                ConsumableFoodInstance instance = diet.getSlots().stream()
                        .min(Comparator.comparingInt(x -> x.duration - x.time))
                        .orElse(null);

                if (instance != null) {
                    diet.removeFromSlot(player, instance);
                    ConsumableFood food = new ConsumableFood(item.getFoodProperties());
                    diet.addToSlot(player, food.create(item));
                }
            }
        }
    }
}