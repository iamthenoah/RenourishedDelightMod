package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.Configuration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class ConfigMenuScreen extends Screen {

    private final @Nullable Screen parent;

    public ConfigMenuScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 37;

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.client"),
                        button -> minecraft.setScreen(AutoConfig.getConfigScreen(Configuration.Client.class, this).get()))
                .bounds(centerX - 100, top, 200, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.food_items"),
                        button -> minecraft.setScreen(new FoodItemConfigScreen(this)))
                .bounds(centerX - 100, top + 24, 200, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        button -> minecraft.setScreen(parent))
                .bounds(centerX - 100, top + 54, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 60, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
