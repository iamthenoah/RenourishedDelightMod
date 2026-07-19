package com.than00ber.renourisheddelight.fabric.mixin.client;

import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void renderFood(CallbackInfo callback) {
        callback.cancel();
    }

    @ModifyArg(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/Identifier;IIII)V"), index = 2)
    private int renderPlayerHealth(int y) {
        return Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty() ? y : y + 10;
    }
}
