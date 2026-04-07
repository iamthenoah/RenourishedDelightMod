package com.than00ber.renourisheddelight.food;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum EatingOutcome {
    CONSUME(true, null),
    EFFECTS_ONLY(true, null),
    REPLENISH(true, null),
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

    public Component message() {
        return message != null ? Component.translatable(message) : Component.empty();
    }
    
    public void process(ServerPlayer player, Diet diet, ItemStack stack) {
        switch (this) {
            case CONSUME -> {
                ConsumableFood food = new ConsumableFood(stack.getItem().getFoodProperties());
                diet.addToSlot(player, food.create(stack.getItem()));
            }
            case EFFECTS_ONLY -> {
                FoodProperties properties = stack.getItem().getFoodProperties();

                if (properties != null) {
                    properties.getEffects().forEach(x -> player.addEffect(new MobEffectInstance(x.getFirst())));
                }
            }
            case REPLENISH -> {
                ConsumableFoodInstance instance = diet.getSlots().stream()
                        .filter(x -> x.item == stack.getItem())
                        .findFirst().orElse(null);

                if (instance != null) {
                    ConsumableFood food = new ConsumableFood(stack.getItem().getFoodProperties());
                    ConsumableFoodInstance target = food.create(stack.getItem());
                    instance.time += target.time;
                }
            }
        }
    }
}
