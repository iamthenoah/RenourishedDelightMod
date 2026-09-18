package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
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
        MinecraftServer server = player.getServer();

        if (server != null) {
            FoodConfig config = FoodConfigSavedData.get(server).getFoodConfig();
            FoodProperties properties = item.components().get(DataComponents.FOOD);
            GameRules rules = player.level().getGameRules();

            switch (this) {
                case CONSUME -> diet.addToSlot(player, diet.eat(player, item, config));
                case EFFECTS_ONLY -> {
                    if (properties != null) {
                        properties.effects().forEach(x -> player.addEffect(new MobEffectInstance(x.effect())));
                    }
                    diet.decay(player, item);
                }
                case REPLENISH -> replace(player, diet, config, item, diet.getSlots().stream()
                        .filter(x -> x.item() == item)
                        .findFirst()
                        .orElse(null));
                case REPLACE_LOW -> replace(player, diet, config, item, diet.getSlots().stream()
                        .min(Comparator.comparingInt(x -> x.duration() - x.time()))
                        .orElse(null));
                default -> {
                    // do nothing
                }
            }
            boolean full = diet.getSlots().size() >= Math.max(1, rules.getInt(GameRuleRegistry.MAX_ACTIVE_FOODS));
            boolean harmful = properties != null && properties.effects().stream()
                    .anyMatch(x -> x.effect().getEffect().value().getCategory() == MobEffectCategory.HARMFUL);

            if (nourishable && full && !harmful) {
                diet.nourish(player, rules);
            }
        }
    }

    private static void replace(ServerPlayer player, Diet diet, FoodConfig config, Item item, @Nullable ConsumableFoodInstance instance) {
        if (instance != null) {
            diet.removeFromSlot(player, instance);
            diet.addToSlot(player, diet.eat(player, item, config));
        }
    }
}
