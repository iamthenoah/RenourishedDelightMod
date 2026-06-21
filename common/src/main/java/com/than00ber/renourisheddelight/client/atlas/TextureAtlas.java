package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record TextureAtlas(Map<Item, Texture[]> textures) {

    private static final int ICONS_PER_ROW = 5;
    private static final int MAX_ROWS = 30;

    public @Nullable Texture[] getTextures(Item item) {
        return textures.get(item);
    }

    public static class Builder {

        protected final ResourceLocation name;
        protected final DynamicTexture texture;
        protected final Map<Item, Texture[]> textures = new HashMap<>();

        protected int uOffset;
        protected int vOffset;
        protected int dimensions;

        protected Builder(String name, int dimensions, int count) {
            this.dimensions = dimensions;
            int width = (count / MAX_ROWS + 1) * (dimensions * ICONS_PER_ROW);
            int height = MAX_ROWS * dimensions;
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
            textures.get(item)[index] = new Texture(this, u, v, dimensions);

            if (index == ICONS_PER_ROW - 1) {
                vOffset += input.getHeight();

                if ((vOffset / input.getHeight()) >= MAX_ROWS) {
                    vOffset = 0;
                    uOffset += input.getWidth() * ICONS_PER_ROW;
                }
            }
            return this;
        }

        public TextureAtlas done() {
            texture.upload(); // update texture with image data
            return new TextureAtlas(textures);
        }
    }
}
