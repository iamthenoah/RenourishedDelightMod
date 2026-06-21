package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.core.component.DataComponents;
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
    CONSUME(true, true, null),
    EFFECTS_ONLY(true, false, null),
    REPLENISH(true, true, null),
    REPLACE_LOW(true, true, null),
    TOO_MANY(false, false, "message.eating_too_many"),
    NOT_BALANCED(false, false, "message.eating_not_balanced");

    final boolean success;
    final boolean nourishable;
    final @Nullable String message;

    EatingOutcome(boolean success, boolean nourishable, @Nullable String message) {
        this.success = success;
        this.nourishable = nourishable;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<MutableComponent> message() {
        return message != null ? Optional.of(Component.translatable(message)) : Optional.empty();
    }

    public void consume(ServerPlayer player, Diet diet, Item item) {
        FoodProperties properties = item.components().get(DataComponents.FOOD);

        switch (this) {
            case CONSUME -> {
                ConsumableFood food = new ConsumableFood(properties);
                diet.addToSlot(player, food.create(item));
            }
            case EFFECTS_ONLY -> {
                if (properties != null) {
                    properties.effects().forEach(x -> player.addEffect(new MobEffectInstance(x.effect())));
                }
            }
            case REPLENISH -> {
                ConsumableFoodInstance instance = diet.getSlots().stream()
                        .filter(x -> x.item == item)
                        .findFirst()
                        .orElse(null);

                if (instance != null) {
                    ConsumableFood food = new ConsumableFood(properties);
                    instance.time -= Math.max(0, food.create(item).duration);
                }
            }
            case REPLACE_LOW -> {
                ConsumableFoodInstance instance = diet.getSlots().stream()
                        .min(Comparator.comparingInt(x -> x.duration - x.time))
                        .orElse(null);

                if (instance != null) {
                    diet.removeFromSlot(player, instance);
                    ConsumableFood food = new ConsumableFood(properties);
                    diet.addToSlot(player, food.create(item));
                }
            }
        }
        if (nourishable) {
            int maxSlots = player.level().getGameRules().getInt(GameRuleRegistry.MAX_CONSUMABLE_FOOD);

            if (diet.getSlots().size() >= maxSlots) {
                int smallest = diet.getSlots().stream().mapToInt(slot -> slot.duration).min().orElse(0);
                double percent = Configuration.Common.getInstance().nourishmentDurationPercent;
                int duration = Math.toIntExact(Math.round(smallest * percent));

                if (duration > 0) {
                    player.addEffect(new MobEffectInstance(EffectRegistry.NOURISHMENT, duration, 0, false, false, true));
                }
            }
        }
    }
}