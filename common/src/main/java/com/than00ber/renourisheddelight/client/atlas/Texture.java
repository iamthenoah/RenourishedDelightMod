package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record Texture(Identifier name, DynamicTexture texture, int u, int v, int dimensions) {

    public void render(GuiGraphicsExtractor graphics, int x, int y, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        RenderSystem.setShaderLights(r, g, b, a);
        NativeImage image = Objects.requireNonNull(texture.getPixels());
        graphics.blit(name, x, y, u, v, dimensions, dimensions, image.getWidth(), image.getHeight());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}