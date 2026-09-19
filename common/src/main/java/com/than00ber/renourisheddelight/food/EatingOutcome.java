package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;

import java.util.Comparator;

public enum EatingOutcome {
    CONSUME,
    REPLENISH,
    REPLACE_LOW;

    public void consume(ServerPlayer player, Diet diet, Item item) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        FoodConfig config = FoodConfigSavedData.get(server).getFoodConfig();
        GameRules rules = player.level().getGameRules();

        switch (this) {
            case CONSUME -> diet.addToSlot(player, diet.eat(player, item, config));
            case REPLENISH -> diet.replaceSlot(player, config, item, diet.getSlots().stream()
                    .filter(x -> x.item() == item)
                    .findFirst()
                    .orElse(null));
            case REPLACE_LOW -> diet.replaceSlot(player, config, item, diet.getSlots().stream()
                    .min(Comparator.comparingInt(x -> x.duration() - x.time()))
                    .orElse(null));
        }
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        boolean full = diet.getSlots().size() >= Math.max(1, rules.getInt(GameRuleRegistry.MAX_ACTIVE_FOODS));
        boolean harmful = properties != null && properties.effects().stream()
                .anyMatch(x -> x.effect().getEffect().value().getCategory() == MobEffectCategory.HARMFUL);

        if (full && !harmful) {
            diet.nourish(player, rules);
        }
    }
}
