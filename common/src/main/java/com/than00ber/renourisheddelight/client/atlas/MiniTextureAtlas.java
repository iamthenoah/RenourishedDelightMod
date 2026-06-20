package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record MiniTextureAtlas(Map<Item, MiniTexture[]> textures) {

    private static final int ICONS_PER_ROW = 5;
    private static final int MAX_ROWS = 30;

    public MiniTexture[] getTextures(Item item) {
        return textures.get(item);
    }

    public static class Builder {

        protected final ResourceLocation name;
        protected final DynamicTexture texture;
        protected final Map<Item, MiniTexture[]> textures = new HashMap<>();

        protected int uOffset;
        protected int vOffset;

        protected Builder(int count) {
            int width = (count / MAX_ROWS + 1) * (MiniTexture.DIMENSIONS * ICONS_PER_ROW);
            int height = MAX_ROWS * MiniTexture.DIMENSIONS;
            this.texture = new DynamicTexture(new NativeImage(width, height, true));
            this.name = Minecraft.getInstance().getTextureManager().register("mini", texture);
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
            textures.putIfAbsent(item, new MiniTexture[ICONS_PER_ROW]);
            textures.get(item)[index] = new MiniTexture(this, u, v);

            if (index == ICONS_PER_ROW - 1) {
                vOffset += input.getHeight();

                if ((vOffset / input.getHeight()) >= MAX_ROWS) {
                    vOffset = 0;
                    uOffset += input.getWidth() * ICONS_PER_ROW;
                }
            }
            return this;
        }

        public MiniTextureAtlas done() {
            texture.upload(); // update texture with image data
            return new MiniTextureAtlas(textures);
        }
    }
}
