package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.ServerConfiguration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class ServerConfigScreen extends AbstractMenuScreen {

    private static final ServerConfiguration DEFAULTS = new ServerConfiguration();

    private static final int FIELD_WIDTH = 210;
    private static final int RESET_GAP = 5;
    private static final int RESET_WIDTH = 45;
    private static final int TOTAL_WIDTH = FIELD_WIDTH + RESET_GAP + RESET_WIDTH;

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
    protected int contentWidth() {
        return TOTAL_WIDTH;
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

        addToggle(
                left,
                top,
                "text.autoconfig.renourisheddelight/server.option.doNutritionDecay",
                doNutritionDecayValue,
                DEFAULTS.doNutritionDecay,
                value -> doNutritionDecayValue = value);

        addToggle(
                left,
                top + ROW_HEIGHT,
                "text.autoconfig.renourisheddelight/server.option.doSleepFoodDrain",
                doSleepFoodDrainValue,
                DEFAULTS.doSleepFoodDrain,
                value -> doSleepFoodDrainValue = value);

        addToggle(
                left,
                top + ROW_HEIGHT * 2,
                "text.autoconfig.renourisheddelight/server.option.doNourishment",
                doNourishmentValue,
                DEFAULTS.doNourishment,
                value -> doNourishmentValue = value);

        addToggle(
                left,
                top + ROW_HEIGHT * 3,
                "text.autoconfig.renourisheddelight/server.option.doStarvation",
                doStarvationValue,
                DEFAULTS.doStarvation,
                value -> doStarvationValue = value);

        addToggle(
                left,
                top + ROW_HEIGHT * 4,
                "text.autoconfig.renourisheddelight/server.option.doPreventSprint",
                doPreventSprintValue,
                DEFAULTS.doPreventSprint,
                value -> doPreventSprintValue = value);

        playerStartingHealthField = addIntegerField(
                left,
                top + ROW_HEIGHT * 5,
                "text.autoconfig.renourisheddelight/server.option.playerStartingHealth",
                config.playerStartingHealth,
                DEFAULTS.playerStartingHealth);

        maxConsumableFoodField = addIntegerField(
                left,
                top + ROW_HEIGHT * 6,
                "text.autoconfig.renourisheddelight/server.option.maxConsumableFood",
                config.maxConsumableFood,
                DEFAULTS.maxConsumableFood);

        foodDrainRateField = addIntegerField(
                left,
                top + ROW_HEIGHT * 7,
                "text.autoconfig.renourisheddelight/server.option.foodDrainRate",
                config.foodDrainRate,
                DEFAULTS.foodDrainRate);

        regenHealthTickIntervalField = addIntegerField(
                left,
                top + ROW_HEIGHT * 8,
                "text.autoconfig.renourisheddelight/server.option.regenHealthTickInterval",
                config.regenHealthTickInterval,
                DEFAULTS.regenHealthTickInterval);

        regenDelayAfterDamageField = addIntegerField(
                left,
                top + ROW_HEIGHT * 9,
                "text.autoconfig.renourisheddelight/server.option.regenDelayAfterDamage",
                config.regenDelayAfterDamage,
                DEFAULTS.regenDelayAfterDamage);

        nutritionDecayRateField = addIntegerField(
                left,
                top + ROW_HEIGHT * 10,
                "text.autoconfig.renourisheddelight/server.option.nutritionDecayRate",
                config.nutritionDecayRate,
                DEFAULTS.nutritionDecayRate);

        nutritionDecayWindowField = addIntegerField(
                left,
                top + ROW_HEIGHT * 11,
                "text.autoconfig.renourisheddelight/server.option.nutritionDecayWindow",
                config.nutritionDecayWindow,
                DEFAULTS.nutritionDecayWindow);

        nutritionDecayFloorField = addIntegerField(
                left,
                top + ROW_HEIGHT * 12,
                "text.autoconfig.renourisheddelight/server.option.nutritionDecayFloor",
                config.nutritionDecayFloor,
                DEFAULTS.nutritionDecayFloor);

        addDoneButton(left, top + ROW_HEIGHT * 13 + DONE_BUTTON_GAP);
    }

    private void addResetButton(
            int x,
            int y,
            int fieldWidth,
            Runnable action) {
        addRenderableWidget(
                Button.builder(
                        Component.translatable("config.renourisheddelight.reset"),
                        button -> action.run())
                        .bounds(
                                x + fieldWidth + RESET_GAP,
                                y,
                                RESET_WIDTH,
                                BUTTON_HEIGHT)
                        .build());
    }

    private void addToggle(
            int x,
            int y,
            String labelKey,
            boolean initial,
            boolean defaultValue,
            Consumer<Boolean> onChange) {
        boolean[] state = { initial };

        Button toggleButton = Button.builder(
                toggleLabel(labelKey, initial),
                button -> {
                    state[0] = !state[0];
                    onChange.accept(state[0]);
                    button.setMessage(toggleLabel(labelKey, state[0]));
                }).bounds(x, y, FIELD_WIDTH, BUTTON_HEIGHT).build();

        toggleButton.setTooltip(
                Tooltip.create(
                        Component.translatable(labelKey + ".@Tooltip")));

        addRenderableWidget(toggleButton);

        addResetButton(x, y, FIELD_WIDTH, () -> {
            state[0] = defaultValue;
            onChange.accept(defaultValue);
            toggleButton.setMessage(toggleLabel(labelKey, defaultValue));
        });
    }

    private static final int INTEGER_FIELD_WIDTH = 40;
    private static final int LABEL_GAP = 8;

    private EditBox addIntegerField(
            int x,
            int y,
            String labelKey,
            int initial,
            int defaultValue) {
        int fieldX = x + FIELD_WIDTH - INTEGER_FIELD_WIDTH;

        Component label = Component.translatable(labelKey);

        StringWidget textLabel = new StringWidget(
                x,
                y,
                FIELD_WIDTH - INTEGER_FIELD_WIDTH - LABEL_GAP,
                BUTTON_HEIGHT,
                label,
                font).alignLeft();

        EditBox field = new EditBox(
                font,
                fieldX,
                y,
                INTEGER_FIELD_WIDTH,
                BUTTON_HEIGHT,
                label);

        field.setMaxLength(10);
        field.setValue(String.valueOf(initial));

        textLabel.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip")));
        field.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip")));

        addRenderableWidget(textLabel);
        addRenderableWidget(field);

        addResetButton(fieldX, y, INTEGER_FIELD_WIDTH, () -> field.setValue(String.valueOf(defaultValue)));

        return field;
    }

    private Component toggleLabel(String labelKey, boolean value) {
        Component valueText = Component.translatable(
                value ? "gui.yes" : "gui.no").withStyle(
                        value ? ChatFormatting.GREEN : ChatFormatting.RED);

        return Component.translatable(labelKey)
                .append(Component.literal(": "))
                .append(valueText);
    }

    private int parseInteger(EditBox field, int fallback) {
        try {
            return Integer.parseInt(field.getValue().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    protected void save() {
        ServerConfiguration config = ServerConfiguration.getInstance();

        config.doNutritionDecay = doNutritionDecayValue;
        config.doSleepFoodDrain = doSleepFoodDrainValue;
        config.doNourishment = doNourishmentValue;
        config.doStarvation = doStarvationValue;
        config.doPreventSprint = doPreventSprintValue;

        config.playerStartingHealth = parseInteger(
                playerStartingHealthField,
                DEFAULTS.playerStartingHealth);

        config.maxConsumableFood = parseInteger(
                maxConsumableFoodField,
                DEFAULTS.maxConsumableFood);

        config.foodDrainRate = parseInteger(
                foodDrainRateField,
                DEFAULTS.foodDrainRate);

        config.regenHealthTickInterval = parseInteger(
                regenHealthTickIntervalField,
                DEFAULTS.regenHealthTickInterval);

        config.regenDelayAfterDamage = parseInteger(
                regenDelayAfterDamageField,
                DEFAULTS.regenDelayAfterDamage);

        config.nutritionDecayRate = parseInteger(
                nutritionDecayRateField,
                DEFAULTS.nutritionDecayRate);

        config.nutritionDecayWindow = parseInteger(
                nutritionDecayWindowField,
                DEFAULTS.nutritionDecayWindow);

        config.nutritionDecayFloor = parseInteger(
                nutritionDecayFloorField,
                DEFAULTS.nutritionDecayFloor);

        AutoConfig.getConfigHolder(ServerConfiguration.class).save();
    }
}