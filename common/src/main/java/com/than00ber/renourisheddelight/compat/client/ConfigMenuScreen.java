package com.than00ber.renourisheddelight.compat.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class ConfigMenuScreen extends AbstractMenuScreen {

    private static final int TOTAL_WIDTH = 260;
    private static final int ROWS = 5;

    public ConfigMenuScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.title"), parent);
    }

    @Override
    protected int contentWidth() {
        return TOTAL_WIDTH;
    }

    @Override
    protected void init() {
        layout(ROWS);
        int left = contentLeft();

        addLink(left, top, "config.renourisheddelight.food_items", () -> new FoodItemConfigScreen(this));
        addLink(left, top + ROW_HEIGHT, "config.renourisheddelight.duration_multipliers", () -> new DurationMultiplierScreen(this));
        addLink(left, top + ROW_HEIGHT * 2, "config.renourisheddelight.starvation", () -> new StarvationScreen(this));
        addLink(left, top + ROW_HEIGHT * 3, "config.renourisheddelight.client", () -> new ClientSettingsScreen(this));
        addDoneButton(left, top + ROW_HEIGHT * (ROWS - 1) + DONE_BUTTON_GAP);
    }

    private void addLink(int x, int y, String labelKey, Supplier<Screen> screen) {
        addRenderableWidget(Button.builder(Component.translatable(labelKey), button -> minecraft.setScreen(screen.get()))
                .bounds(x, y, TOTAL_WIDTH, BUTTON_HEIGHT)
                .build());
    }
}
