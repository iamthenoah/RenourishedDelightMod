package com.than00ber.renourisheddelight.compat.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class AbstractMenuScreen extends Screen {

    protected static final int ROW_HEIGHT = 24;
    protected static final int BUTTON_HEIGHT = 20;
    protected static final int DONE_BUTTON_GAP = 10;
    protected static final int TITLE_GAP = 18;
    protected static final int FIELD_WIDTH = 210;
    protected static final int RESET_GAP = 5;
    protected static final int RESET_WIDTH = 45;
    protected static final int TOTAL_WIDTH = FIELD_WIDTH + RESET_GAP + RESET_WIDTH;
    protected static final int INTEGER_FIELD_WIDTH = 40;
    protected static final int LABEL_GAP = 8;

    private final @Nullable Screen parent;

    protected int top;

    protected AbstractMenuScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
    }

    protected int contentWidth() {
        return TOTAL_WIDTH;
    }

    protected int contentLeft() {
        return width / 2 - contentWidth() / 2;
    }

    protected void layout(int rows) {
        top = height / 2 - (ROW_HEIGHT * rows) / 2;
    }

    protected void addDoneButton(int x, int y) {
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, y, contentWidth(), BUTTON_HEIGHT)
                .build());
    }

    protected void addToggle(int x, int y, String labelKey, boolean initial, boolean defaultValue, Consumer<Boolean> onChange) {
        addToggle(x, y, FIELD_WIDTH, labelKey, initial, defaultValue, onChange);
    }

    protected void addToggle(int x, int y, int width, String labelKey, boolean initial, boolean defaultValue, Consumer<Boolean> onChange) {
        boolean[] state = { initial };
        Button toggleButton = Button.builder(toggleLabel(labelKey, initial), button -> {
            state[0] = !state[0];
            onChange.accept(state[0]);
            button.setMessage(toggleLabel(labelKey, state[0]));
        }).bounds(x, y, width, BUTTON_HEIGHT).build();
        toggleButton.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip")));
        addRenderableWidget(toggleButton);
        addResetButton(x, y, width, () -> {
            state[0] = defaultValue;
            onChange.accept(defaultValue);
            toggleButton.setMessage(toggleLabel(labelKey, defaultValue));
        });
    }

    protected EditBox addIntegerField(int x, int y, String labelKey, int initial, int defaultValue) {
        int fieldX = x + FIELD_WIDTH - INTEGER_FIELD_WIDTH;
        Component label = Component.translatable(labelKey);

        StringWidget textLabel = new StringWidget(x, y, FIELD_WIDTH - INTEGER_FIELD_WIDTH - LABEL_GAP, BUTTON_HEIGHT, label, font).alignLeft();
        textLabel.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip")));

        EditBox field = new EditBox(font, fieldX, y, INTEGER_FIELD_WIDTH, BUTTON_HEIGHT, label);
        field.setMaxLength(10);
        field.setValue(String.valueOf(initial));
        field.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip")));

        addRenderableWidget(textLabel);
        addRenderableWidget(field);
        addResetButton(fieldX, y, INTEGER_FIELD_WIDTH, () -> field.setValue(String.valueOf(defaultValue)));
        return field;
    }

    // the range stretches to fit a larger value set in the config file, so opening the screen never clamps it
    protected IntegerSlider addIntegerSlider(int x, int y, int width, String labelKey, int initial, int defaultValue, int min, int max, String suffix) {
        IntegerSlider slider = new IntegerSlider(x, y, width, Component.translatable(labelKey), suffix, Math.min(min, initial), Math.max(max, initial), initial);
        slider.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip")));
        addRenderableWidget(slider);
        addResetButton(x, y, width, () -> slider.setInt(defaultValue));
        return slider;
    }

    protected void addResetButton(int x, int y, int fieldWidth, Runnable action) {
        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.reset"), button -> action.run())
                .bounds(x + fieldWidth + RESET_GAP, y, RESET_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    protected Component toggleLabel(String labelKey, boolean value) {
        Component valueText = Component
                .translatable(value ? "gui.yes" : "gui.no")
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
        return Component.translatable(labelKey)
                .append(Component.literal(": "))
                .append(valueText);
    }

    protected int parseInteger(EditBox field, int fallback) {
        try {
            return Integer.parseInt(field.getValue().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    protected void save() {
    }

    protected static final class IntegerSlider extends AbstractSliderButton {

        private final Component label;
        private final String suffix;
        private final int min;
        private final int max;
        private int current;

        private IntegerSlider(int x, int y, int width, Component label, String suffix, int min, int max, int initial) {
            super(x, y, width, BUTTON_HEIGHT, Component.empty(), 0.0);
            this.label = label;
            this.suffix = suffix;
            this.min = min;
            this.max = max;
            setInt(initial);
        }

        public int getInt() {
            return current;
        }

        public void setInt(int value) {
            current = Mth.clamp(value, min, max);
            this.value = max > min ? (current - min) / (double) (max - min) : 0.0;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(label.copy().append(Component.literal(": " + current + suffix)));
        }

        @Override
        protected void applyValue() {
            current = min + (int) Math.round(value * (max - min));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, top - TITLE_GAP, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        save();
        minecraft.setScreen(parent);
    }
}
