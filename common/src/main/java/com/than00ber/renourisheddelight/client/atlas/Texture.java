package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Objects;

public record Texture(AtlasHandle handle, int u, int v, int dimensions) {

    public void render(GuiGraphics graphics, int x, int y, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        RenderSystem.setShaderColor(r, g, b, a);
        NativeImage texture = Objects.requireNonNull(handle.image());
        graphics.blit(handle.name(), x, y, u, v, dimensions, dimensions, texture.getWidth(), texture.getHeight());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}