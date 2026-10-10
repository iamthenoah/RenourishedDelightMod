package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.ServerConfiguration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class ServerConfigScreen extends AbstractMenuScreen {

    private static final ServerConfiguration DEFAULTS = new ServerConfiguration();
    private static final int ROWS = 14;

    private boolean doNutritionDecayValue;
    private boolean doSleepFoodDrainValue;
    private boolean doNourishmentValue;
    private boolean doStarvationValue;
    private boolean doPreventSprintValue;

    private EditBox playerStartingHealthField;
    private EditBox maxConsumableFoodField;
    private EditBox foodDrainRateField;
    private EditBox regenHealthTickIntervalField;
    private EditBox regenDelayAfterDamageField;
    private EditBox nutritionDecayRateField;
    private EditBox nutritionDecayWindowField;
    private EditBox nutritionDecayFloorField;

    public ServerConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.server"), parent);
    }

    @Override
    protected void init() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        doNutritionDecayValue = config.doNutritionDecay;
        doSleepFoodDrainValue = config.doSleepFoodDrain;
        doNourishmentValue = config.doNourishment;
        doStarvationValue = config.doStarvation;
        doPreventSprintValue = config.doPreventSprint;

        layout(ROWS);
        int left = contentLeft();

        addToggle(left, top, "text.autoconfig.renourisheddelight/server.option.doNutritionDecay", doNutritionDecayValue, DEFAULTS.doNutritionDecay, value -> doNutritionDecayValue = value);
        addToggle(left, top + ROW_HEIGHT, "text.autoconfig.renourisheddelight/server.option.doSleepFoodDrain", doSleepFoodDrainValue, DEFAULTS.doSleepFoodDrain, value -> doSleepFoodDrainValue = value);
        addToggle(left, top + ROW_HEIGHT * 2, "text.autoconfig.renourisheddelight/server.option.doNourishment", doNourishmentValue, DEFAULTS.doNourishment, value -> doNourishmentValue = value);
        addToggle(left, top + ROW_HEIGHT * 3, "text.autoconfig.renourisheddelight/server.option.doStarvation", doStarvationValue, DEFAULTS.doStarvation, value -> doStarvationValue = value);
        addToggle(left, top + ROW_HEIGHT * 4, "text.autoconfig.renourisheddelight/server.option.doPreventSprint", doPreventSprintValue, DEFAULTS.doPreventSprint, value -> doPreventSprintValue = value);

        playerStartingHealthField = addIntegerField(left, top + ROW_HEIGHT * 5, "text.autoconfig.renourisheddelight/server.option.playerStartingHealth", config.playerStartingHealth, DEFAULTS.playerStartingHealth);
        maxConsumableFoodField = addIntegerField(left, top + ROW_HEIGHT * 6, "text.autoconfig.renourisheddelight/server.option.maxConsumableFood", config.maxConsumableFood, DEFAULTS.maxConsumableFood);
        foodDrainRateField = addIntegerField(left, top + ROW_HEIGHT * 7, "text.autoconfig.renourisheddelight/server.option.foodDrainRate", config.foodDrainRate, DEFAULTS.foodDrainRate);
        regenHealthTickIntervalField = addIntegerField(left, top + ROW_HEIGHT * 8, "text.autoconfig.renourisheddelight/server.option.regenHealthTickInterval", config.regenHealthTickInterval, DEFAULTS.regenHealthTickInterval);
        regenDelayAfterDamageField = addIntegerField(left, top + ROW_HEIGHT * 9, "text.autoconfig.renourisheddelight/server.option.regenDelayAfterDamage", config.regenDelayAfterDamage, DEFAULTS.regenDelayAfterDamage);
        nutritionDecayRateField = addIntegerField(left, top + ROW_HEIGHT * 10, "text.autoconfig.renourisheddelight/server.option.nutritionDecayRate", config.nutritionDecayRate, DEFAULTS.nutritionDecayRate);
        nutritionDecayWindowField = addIntegerField(left, top + ROW_HEIGHT * 11, "text.autoconfig.renourisheddelight/server.option.nutritionDecayWindow", config.nutritionDecayWindow, DEFAULTS.nutritionDecayWindow);
        nutritionDecayFloorField = addIntegerField(left, top + ROW_HEIGHT * 12, "text.autoconfig.renourisheddelight/server.option.nutritionDecayFloor", config.nutritionDecayFloor, DEFAULTS.nutritionDecayFloor);

        addDoneButton(left, top + ROW_HEIGHT * 13 + DONE_BUTTON_GAP);
    }

    @Override
    protected void save() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        config.doNutritionDecay = doNutritionDecayValue;
        config.doSleepFoodDrain = doSleepFoodDrainValue;
        config.doNourishment = doNourishmentValue;
        config.doStarvation = doStarvationValue;
        config.doPreventSprint = doPreventSprintValue;
        config.playerStartingHealth = parseInteger(playerStartingHealthField, DEFAULTS.playerStartingHealth);
        config.maxConsumableFood = parseInteger(maxConsumableFoodField, DEFAULTS.maxConsumableFood);
        config.foodDrainRate = parseInteger(foodDrainRateField, DEFAULTS.foodDrainRate);
        config.regenHealthTickInterval = parseInteger(regenHealthTickIntervalField, DEFAULTS.regenHealthTickInterval);
        config.regenDelayAfterDamage = parseInteger(regenDelayAfterDamageField, DEFAULTS.regenDelayAfterDamage);
        config.nutritionDecayRate = parseInteger(nutritionDecayRateField, DEFAULTS.nutritionDecayRate);
        config.nutritionDecayWindow = parseInteger(nutritionDecayWindowField, DEFAULTS.nutritionDecayWindow);
        config.nutritionDecayFloor = parseInteger(nutritionDecayFloorField, DEFAULTS.nutritionDecayFloor);
        AutoConfig.getConfigHolder(ServerConfiguration.class).save();
    }
}
