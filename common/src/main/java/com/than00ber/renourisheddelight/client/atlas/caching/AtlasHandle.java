package com.than00ber.renourisheddelight.client.atlas.caching;

import com.mojang.blaze3d.platform.NativeImage;
import com.than00ber.renourisheddelight.client.atlas.Texture;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlas;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Everything a {@link Texture} needs in order to blit itself: the registered GPU resource
 * location and the backing {@link DynamicTexture}. Decoupled from {@link TextureAtlas.Builder}
 * so that atlases loaded straight from the on-disk cache (see {@link AtlasCache}) don't need to
 * replay the whole build process just to get something renderable.
 */
public record AtlasHandle(ResourceLocation name, DynamicTexture texture) {

    public NativeImage pixels() {
        return texture.getPixels();
    }
}
