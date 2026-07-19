package com.than00ber.renourisheddelight.registry;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

public final class GameRuleRegistry {

    public static void init() {
        // do nothing
    }

    private static final GameRuleCategory CATEGORY = new GameRuleCategory(Identifier.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "category"));

    public static final GameRule<Integer> PLAYER_STARTING_HEARTS = GameRules.registerInteger("playerStartingHearts", CATEGORY, 20, 1, Integer.MAX_VALUE);
    public static final GameRule<Integer> MAX_CONSUMABLE_FOOD = GameRules.registerInteger("maxConsumableFood", CATEGORY, 3, 1, 10);
    public static final GameRule<Boolean> ALLOW_EATING_SAME_ITEM = GameRules.registerBoolean("allowEatingTheSameItem", CATEGORY, false);
    public static final GameRule<Boolean> REPLACE_LOWEST_FOOD_ITEM = GameRules.registerBoolean("replaceLowestFoodItem", CATEGORY, false);
    public static final GameRule<Integer> FOOD_REPLENISHABLE_THRESHOLD = GameRules.registerInteger("foodReplenishableThreshold", CATEGORY, 50, 0, 100);
    public static final GameRule<Boolean> FOOD_ITEM_STACKS = GameRules.registerBoolean("foodItemStacks", CATEGORY, true);
    public static final GameRule<Integer> HUNGER_FOOD_DRAIN = GameRules.registerInteger("hungerFoodDrain", CATEGORY, 2, 0, Integer.MAX_VALUE);
    public static final GameRule<Integer> REGEN_HEALTH_TICK_INTERVAL = GameRules.registerInteger("regenHealthTickInterval", CATEGORY, 60, 1, Integer.MAX_VALUE);
    public static final GameRule<Integer> REGEN_HEALTH_FOOD_DRAIN = GameRules.registerInteger("regenHealthFoodDrain", CATEGORY, 3, 1, Integer.MAX_VALUE);
    public static final GameRule<Boolean> APPLY_NOURISHMENT_WHEN_FULL = GameRules.registerBoolean("applyNourishmentWhenFull", CATEGORY, true);
    public static final GameRule<Integer> NOURISHMENT_DURATION_PERCENT = GameRules.registerInteger("nourishmentDurationPercent", CATEGORY, 10, 0, 100);
    public static final GameRule<Integer> REGEN_DELAY_AFTER_DAMAGE = GameRules.registerInteger("regenDelayAfterDamage", CATEGORY, 60, 0, Integer.MAX_VALUE);
    public static final GameRule<Integer> SLEEP_FOOD_DRAIN = GameRules.registerInteger("sleepFoodDrain", CATEGORY, 12000, 0, Integer.MAX_VALUE);
    public static final GameRule<Integer> ATTACK_FOOD_DRAIN = GameRules.registerInteger("attackFoodDrain", CATEGORY, 0, 0, Integer.MAX_VALUE);
    public static final GameRule<Integer> JUMP_FOOD_DRAIN = GameRules.registerInteger("jumpFoodDrain", CATEGORY, 0, 0, Integer.MAX_VALUE);
    public static final GameRule<Integer> SPRINT_FOOD_DRAIN = GameRules.registerInteger("sprintFoodDrain", CATEGORY, 0, 0, Integer.MAX_VALUE);
    public static final GameRule<Boolean> DISABLE_HEALTH_REGEN_WHEN_HUNGRY = GameRules.registerBoolean("disableHealthRegenWhenHungry", CATEGORY, true);
    public static final GameRule<Integer> NOURISHMENT_REGEN_TICK_INTERVAL = GameRules.registerInteger("nourishmentRegenTickInterval", CATEGORY, 20, 0, Integer.MAX_VALUE);
}
