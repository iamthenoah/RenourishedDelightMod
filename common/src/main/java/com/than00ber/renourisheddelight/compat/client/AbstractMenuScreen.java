package com.than00ber.renourisheddelight.compat.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMenuScreen extends Screen {

    protected static final int ROW_HEIGHT = 24;
    protected static final int BUTTON_HEIGHT = 20;
    protected static final int DONE_BUTTON_GAP = 10;
    protected static final int TITLE_GAP = 18;

    private final @Nullable Screen parent;

    protected int top;

    protected AbstractMenuScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
    }

    protected abstract int contentWidth();

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

    protected void save() {
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
