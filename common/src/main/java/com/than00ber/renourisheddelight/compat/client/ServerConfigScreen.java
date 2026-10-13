package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.ServerConfiguration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class ServerConfigScreen extends AbstractMenuScreen {

    private static final ServerConfiguration DEFAULTS = new ServerConfiguration();
    private static final String KEY = "text.autoconfig.renourisheddelight/server.option.";
    private static final int ROWS = 9;
    private static final int COLUMN_GAP = 10;
    private static final int SIDE_MARGIN = 10;

    private boolean doNutritionDecayValue;
    private boolean doSleepFoodDrainValue;
    private boolean doNourishmentValue;
    private boolean doStarvationValue;
    private boolean doPreventSprintValue;
    private boolean doReplaceLowestFoodValue;
    private boolean doFoodCategoriesValue;

    private IntegerSlider playerStartingHealthSlider;
    private IntegerSlider maxConsumableFoodSlider;
    private IntegerSlider foodDrainRateSlider;
    private IntegerSlider regenHealthTickIntervalSlider;
    private IntegerSlider regenDelayAfterDamageSlider;
    private IntegerSlider nutritionDecayRateSlider;
    private IntegerSlider nutritionDecayWindowSlider;
    private IntegerSlider nutritionDecayFloorSlider;

    public ServerConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.server"), parent);
    }

    // shrinks to fit side by side on narrow (high gui scale) windows
    private int columnWidth() {
        return Math.min(TOTAL_WIDTH, (width - SIDE_MARGIN * 2 - COLUMN_GAP) / 2);
    }

    @Override
    protected int contentWidth() {
        return columnWidth() * 2 + COLUMN_GAP;
    }

    @Override
    protected void init() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        doNutritionDecayValue = config.doNutritionDecay;
        doSleepFoodDrainValue = config.doSleepFoodDrain;
        doNourishmentValue = config.doNourishment;
        doStarvationValue = config.doStarvation;
        doPreventSprintValue = config.doPreventSprint;
        doReplaceLowestFoodValue = config.doReplaceLowestFood;
        doFoodCategoriesValue = config.doFoodCategories;

        layout(ROWS);
        int left = contentLeft();
        int right = left + columnWidth() + COLUMN_GAP;
        int field = columnWidth() - RESET_GAP - RESET_WIDTH;

        addToggle(left, top, field, KEY + "doNutritionDecay", doNutritionDecayValue, DEFAULTS.doNutritionDecay, value -> doNutritionDecayValue = value);
        addToggle(left, top + ROW_HEIGHT, field, KEY + "doSleepFoodDrain", doSleepFoodDrainValue, DEFAULTS.doSleepFoodDrain, value -> doSleepFoodDrainValue = value);
        addToggle(left, top + ROW_HEIGHT * 2, field, KEY + "doNourishment", doNourishmentValue, DEFAULTS.doNourishment, value -> doNourishmentValue = value);
        addToggle(left, top + ROW_HEIGHT * 3, field, KEY + "doStarvation", doStarvationValue, DEFAULTS.doStarvation, value -> doStarvationValue = value);
        addToggle(left, top + ROW_HEIGHT * 4, field, KEY + "doPreventSprint", doPreventSprintValue, DEFAULTS.doPreventSprint, value -> doPreventSprintValue = value);
        addToggle(left, top + ROW_HEIGHT * 5, field, KEY + "doReplaceLowestFood", doReplaceLowestFoodValue, DEFAULTS.doReplaceLowestFood, value -> doReplaceLowestFoodValue = value);
        addToggle(left, top + ROW_HEIGHT * 6, field, KEY + "doFoodCategories", doFoodCategoriesValue, DEFAULTS.doFoodCategories, value -> doFoodCategoriesValue = value);

        playerStartingHealthSlider = addIntegerSlider(right, top, field, KEY + "playerStartingHealth", config.playerStartingHealth, DEFAULTS.playerStartingHealth, 2, 40, "");
        maxConsumableFoodSlider = addIntegerSlider(right, top + ROW_HEIGHT, field, KEY + "maxConsumableFood", config.maxConsumableFood, DEFAULTS.maxConsumableFood, 1, 10, "");
        foodDrainRateSlider = addIntegerSlider(right, top + ROW_HEIGHT * 2, field, KEY + "foodDrainRate", config.foodDrainRate, DEFAULTS.foodDrainRate, 0, 300, "%");
        regenHealthTickIntervalSlider = addIntegerSlider(right, top + ROW_HEIGHT * 3, field, KEY + "regenHealthTickInterval", config.regenHealthTickInterval, DEFAULTS.regenHealthTickInterval, 1, 200, "");
        regenDelayAfterDamageSlider = addIntegerSlider(right, top + ROW_HEIGHT * 4, field, KEY + "regenDelayAfterDamage", config.regenDelayAfterDamage, DEFAULTS.regenDelayAfterDamage, 0, 200, "");
        nutritionDecayRateSlider = addIntegerSlider(right, top + ROW_HEIGHT * 5, field, KEY + "nutritionDecayRate", config.nutritionDecayRate, DEFAULTS.nutritionDecayRate, 0, 20, "%");
        nutritionDecayWindowSlider = addIntegerSlider(right, top + ROW_HEIGHT * 6, field, KEY + "nutritionDecayWindow", config.nutritionDecayWindow, DEFAULTS.nutritionDecayWindow, 1, 10, "");
        nutritionDecayFloorSlider = addIntegerSlider(right, top + ROW_HEIGHT * 7, field, KEY + "nutritionDecayFloor", config.nutritionDecayFloor, DEFAULTS.nutritionDecayFloor, 0, 100, "%");

        addDoneButton(left, top + ROW_HEIGHT * 8 + DONE_BUTTON_GAP);
    }

    @Override
    protected void save() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        config.doNutritionDecay = doNutritionDecayValue;
        config.doSleepFoodDrain = doSleepFoodDrainValue;
        config.doNourishment = doNourishmentValue;
        config.doStarvation = doStarvationValue;
        config.doPreventSprint = doPreventSprintValue;
        config.doReplaceLowestFood = doReplaceLowestFoodValue;
        config.doFoodCategories = doFoodCategoriesValue;
        config.playerStartingHealth = playerStartingHealthSlider.getInt();
        config.maxConsumableFood = maxConsumableFoodSlider.getInt();
        config.foodDrainRate = foodDrainRateSlider.getInt();
        config.regenHealthTickInterval = regenHealthTickIntervalSlider.getInt();
        config.regenDelayAfterDamage = regenDelayAfterDamageSlider.getInt();
        config.nutritionDecayRate = nutritionDecayRateSlider.getInt();
        config.nutritionDecayWindow = nutritionDecayWindowSlider.getInt();
        config.nutritionDecayFloor = nutritionDecayFloorSlider.getInt();
        AutoConfig.getConfigHolder(ServerConfiguration.class).save();
    }
}
