package com.than00ber.renourisheddelight.registry;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.world.level.GameRules;

public final class GameRuleRegistry {

    public static void init() {
        // do nothing
    }

    public static final GameRules.Key<GameRules.IntegerValue> STARTING_HEARTS = register("playerStartingHearts", GameRules.IntegerValue.create(20));
    public static final GameRules.Key<GameRules.IntegerValue> MAX_ACTIVE_FOODS = register("maxConsumableFood", GameRules.IntegerValue.create(3));
    public static final GameRules.Key<GameRules.IntegerValue> FOOD_DRAIN_RATE = register("foodDrainRate", GameRules.IntegerValue.create(100));
    public static final GameRules.Key<GameRules.IntegerValue> REGEN_INTERVAL = register("regenHealthTickInterval", GameRules.IntegerValue.create(60));
    public static final GameRules.Key<GameRules.IntegerValue> REGEN_DELAY_AFTER_DAMAGE = register("regenDelayAfterDamage", GameRules.IntegerValue.create(60));
    public static final GameRules.Key<GameRules.IntegerValue> NOURISHMENT_DURATION_PERCENT = register("nourishmentDurationPercent", GameRules.IntegerValue.create(10));
    public static final GameRules.Key<GameRules.BooleanValue> DO_SLEEP_FOOD_DRAIN = register("doSleepFoodDrain", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DO_NOURISHMENT = register("doNourishment", GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> DO_STARVATION = register("doStarvation", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DO_REPLENISH = register("doReplenish", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DO_REPLACE_LOWEST = register("doReplaceLowest", GameRules.BooleanValue.create(true));

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String id, GameRules.Type<T> value) {
        return GameRules.register(RenourishedDelightMod.MOD_ID + ":" + id, GameRules.Category.PLAYER, value);
    }
}
