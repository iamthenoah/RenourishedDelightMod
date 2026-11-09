package com.than00ber.renourisheddelight.fabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Farmer's Delight Refabricated draws its nourishment hunger icons over the vanilla food row, which the food bar replaces
@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.client.gui.NourishmentHungerOverlay", remap = false)
public abstract class NourishmentHungerOverlayMixin {

    @Inject(method = "onRenderGuiOverlayPost", at = @At("HEAD"), cancellable = true, require = 0)
    private static void renourisheddelight$onRenderGuiOverlayPost(CallbackInfo callback) {
        callback.cancel();
    }
}
