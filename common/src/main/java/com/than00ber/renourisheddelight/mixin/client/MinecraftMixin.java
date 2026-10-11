package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void renourisheddelight$onGameLoadFinished(CallbackInfo callback) {
        TextureAtlasResourceLoader.getInstance().onGameLoadFinished();
    }
}
