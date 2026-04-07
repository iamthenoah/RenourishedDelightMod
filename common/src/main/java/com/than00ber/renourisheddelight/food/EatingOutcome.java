package com.than00ber.renourisheddelight.food;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum EatingOutcome {
    SUCCESS(true, null),
    SUCCESS_EFFECTS_ONLY(true, null),
    SUCCESS_REPLENISH(true, null),
    TOO_MANY(false, "message.eating_too_many"),
    NOT_BALANCED(false, "message.eating_not_balanced");

    final boolean success;
    final @Nullable String key;

    EatingOutcome(boolean success, @Nullable String key) {
        this.success = success;
        this.key = key;
    }

    public void process(ServerPlayer player, Diet diet, ItemStack stack) {
        switch (this) {
            case SUCCESS -> {
                ConsumableFood food = new ConsumableFood(stack.getItem().getFoodProperties());
                diet.addToSlot(player, food.create(stack.getItem()));
            }
            case SUCCESS_EFFECTS_ONLY -> {
                FoodProperties properties = stack.getItem().getFoodProperties();

                if (properties != null) {
                    properties.getEffects().forEach(x -> player.addEffect(new MobEffectInstance(x.getFirst())));
                }
            }
            case SUCCESS_REPLENISH -> {
                diet.getSlots().removeIf(x -> x.item == stack.getItem());
                ConsumableFood food = new ConsumableFood(stack.getItem().getFoodProperties());
                diet.addToSlot(player, food.create(stack.getItem()));
            }
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public Component message() {
        return key != null ? Component.translatable(key) : Component.empty();
    }
}
