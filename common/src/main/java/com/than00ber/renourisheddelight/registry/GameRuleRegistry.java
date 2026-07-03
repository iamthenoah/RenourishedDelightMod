package com.than00ber.renourisheddelight.registry;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.world.level.GameRules;

public final class GameRuleRegistry {

    public static void init() {
        // do nothing
    }

    public static final GameRules.Key<GameRules.IntegerValue> PLAYER_STARTING_HEARTS = register("playerStartingHearts", GameRules.IntegerValue.create(20));
    public static final GameRules.Key<GameRules.IntegerValue> MAX_CONSUMABLE_FOOD = register("maxConsumableFood", GameRules.IntegerValue.create(3));
    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_EATING_SAME_ITEM = register("allowEatingTheSameItem", GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> REPLACE_LOWEST_FOOD_ITEM = register("replaceLowestFoodItem", GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.IntegerValue> FOOD_REPLENISHABLE_THRESHOLD = register("foodReplenishableThreshold", GameRules.IntegerValue.create(50));
    public static final GameRules.Key<GameRules.BooleanValue> FOOD_ITEM_STACKS = register("foodItemStacks", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.IntegerValue> HUNGER_FOOD_DRAIN = register("hungerFoodDrain", GameRules.IntegerValue.create(2));
    public static final GameRules.Key<GameRules.IntegerValue> REGEN_HEALTH_TICK_INTERVAL = register("regenHealthTickInterval", GameRules.IntegerValue.create(60));
    public static final GameRules.Key<GameRules.IntegerValue> REGEN_HEALTH_FOOD_DRAIN = register("regenHealthFoodDrain", GameRules.IntegerValue.create(3));
    public static final GameRules.Key<GameRules.BooleanValue> APPLY_NOURISHMENT_WHEN_FULL = register("applyNourishmentWhenFull", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.IntegerValue> REGEN_DELAY_AFTER_DAMAGE = register("regenDelayAfterDamage", GameRules.IntegerValue.create(60));
    public static final GameRules.Key<GameRules.IntegerValue> SLEEP_FOOD_DRAIN = register("sleepFoodDrain", GameRules.IntegerValue.create(6000));

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String id, GameRules.Type<T> value) {
        return GameRules.register(RenourishedDelightMod.MOD_ID + ":" + id, GameRules.Category.PLAYER, value);
    }
}
