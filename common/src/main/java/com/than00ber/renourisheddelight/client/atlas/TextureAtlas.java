package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record TextureAtlas(ResourceLocation name, Map<Item, Texture[]> textures) {

    private static final int ICONS_PER_ROW = 5;

    public @Nullable Texture[] getTextures(Item item) {
        return textures.get(item);
    }

    public void release() {
        Minecraft.getInstance().getTextureManager().release(name);
    }

    public static class Builder {

        protected final ResourceLocation name;
        protected final DynamicTexture texture;
        protected final Map<Item, Texture[]> textures = new HashMap<>();

        protected int uOffset;
        protected int vOffset;
        protected int dimensions;
        protected int rows;

        protected Builder(String name, int dimensions, int count) {
            this.dimensions = dimensions;
            this.rows = Math.max(1, (int) Math.ceil(Math.sqrt(count * (double) ICONS_PER_ROW)));
            int columns = Math.max(1, (count + rows - 1) / rows);
            int width = columns * dimensions * ICONS_PER_ROW;
            int height = rows * dimensions;
            int max = RenderSystem.maxSupportedTextureSize();

            if (width > max || height > max) {
                throw new IllegalStateException("atlas of " + width + "x" + height + " exceeds the maximum texture size of " + max);
            }
            this.texture = new DynamicTexture(new NativeImage(width, height, true));
            this.name = Minecraft.getInstance().getTextureManager().register(name, texture);
        }

        public Builder appendTexture(int index, Item item, NativeImage input) {
            NativeImage image = Objects.requireNonNull(texture.getPixels());
            int width = input.getWidth();
            int height = input.getHeight();
            int u = uOffset + index * input.getWidth();
            int v = vOffset;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setPixelRGBA(u + x, v + y, input.getPixelRGBA(x, y));
                }
            }
            textures.putIfAbsent(item, new Texture[ICONS_PER_ROW]);
            textures.get(item)[index] = new Texture(name, texture, u, v, dimensions);

            if (index == ICONS_PER_ROW - 1) {
                vOffset += input.getHeight();

                if ((vOffset / input.getHeight()) >= rows) {
                    vOffset = 0;
                    uOffset += input.getWidth() * ICONS_PER_ROW;
                }
            }
            return this;
        }

        public TextureAtlas done() {
            texture.upload();
            return new TextureAtlas(name, textures);
        }
    }
}
