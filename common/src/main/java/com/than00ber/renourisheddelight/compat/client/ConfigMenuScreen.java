package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.compat.client.food.DurationMultiplierScreen;
import com.than00ber.renourisheddelight.compat.client.food.FoodItemConfigScreen;
import com.than00ber.renourisheddelight.compat.client.food.StarvationScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class ConfigMenuScreen extends AbstractMenuScreen {

    private static final int ROWS = 5;
    private static final int COLUMN_GAP = 4;
    private static final int HALF_WIDTH = (TOTAL_WIDTH - COLUMN_GAP) / 2;

    public ConfigMenuScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.title"), parent);
    }

    @Override
    protected void init() {
        layout(ROWS);
        int left = contentLeft();

        addLink(left, top, HALF_WIDTH, "config.renourisheddelight.client", () -> new ClientSettingsScreen(this));
        addLink(left + TOTAL_WIDTH - HALF_WIDTH, top, HALF_WIDTH, "config.renourisheddelight.server", () -> new ServerConfigScreen(this));

        addLink(left, top + ROW_HEIGHT, TOTAL_WIDTH, "config.renourisheddelight.food_items", () -> new FoodItemConfigScreen(this));
        addLink(left, top + ROW_HEIGHT * 2, TOTAL_WIDTH, "config.renourisheddelight.duration_multipliers", () -> new DurationMultiplierScreen(this));
        addLink(left, top + ROW_HEIGHT * 3, TOTAL_WIDTH, "config.renourisheddelight.starvation", () -> new StarvationScreen(this));

        addDoneButton(left, top + ROW_HEIGHT * (ROWS - 1) + DONE_BUTTON_GAP);
    }

    private void addLink(int x, int y, int width, String labelKey, Supplier<Screen> screen) {
        addRenderableWidget(Button.builder(Component.translatable(labelKey), button -> minecraft.setScreen(screen.get()))
                .bounds(x, y, width, BUTTON_HEIGHT)
                .build());
    }
}
