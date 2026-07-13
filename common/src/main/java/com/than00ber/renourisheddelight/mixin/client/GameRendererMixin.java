package com.than00ber.renourisheddelight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.than00ber.renourisheddelight.network.MaxHealthShrinkAware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void suppressMaxHealthShrinkTilt(PoseStack poseStack, float partialTick, CallbackInfo callback) {
        Entity camera = Minecraft.getInstance().getCameraEntity();

        if (camera instanceof LivingEntity livingEntity && camera instanceof MaxHealthShrinkAware aware && aware.consumeRecentMaxHealthShrink()) {
            livingEntity.hurtTime = 0;
            livingEntity.hurtDuration = 0;
            callback.cancel();
        }
    }
}
