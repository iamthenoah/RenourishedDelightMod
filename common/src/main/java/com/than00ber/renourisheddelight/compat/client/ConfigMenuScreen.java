package com.than00ber.renourisheddelight.compat.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class ConfigMenuScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int TOTAL_WIDTH = 260;
    private static final int DONE_BUTTON_GAP = 10;

    private final @Nullable Screen parent;
    private int top;

    public ConfigMenuScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = width / 2 - TOTAL_WIDTH / 2;
        top = height / 2 - (ROW_HEIGHT * 5) / 2;

        addLink(left, top, "config.renourisheddelight.food_items", () -> new FoodItemConfigScreen(this));
        addLink(left, top + ROW_HEIGHT, "config.renourisheddelight.duration_multipliers", () -> new DurationMultiplierScreen(this));
        addLink(left, top + ROW_HEIGHT * 2, "config.renourisheddelight.starvation", () -> new StarvationScreen(this));
        addLink(left, top + ROW_HEIGHT * 3, "config.renourisheddelight.client", () -> new ClientSettingsScreen(this));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, top + ROW_HEIGHT * 4 + DONE_BUTTON_GAP, TOTAL_WIDTH, 20)
                .build());
    }

    private void addLink(int x, int y, String labelKey, Supplier<Screen> screen) {
        addRenderableWidget(Button.builder(Component.translatable(labelKey), button -> minecraft.setScreen(screen.get()))
                .bounds(x, y, TOTAL_WIDTH, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, top - 18, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
