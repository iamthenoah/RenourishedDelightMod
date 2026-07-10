package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public record AtlasHandle(ResourceLocation name, DynamicTexture texture) {

    public NativeImage image() {
        return texture.getPixels();
    }
}
