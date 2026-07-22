package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.config.ClientConfiguration;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Unique private static final int HEART_SIZE = 9;

    @Unique private boolean renourisheddelight$clipActive;
    @Unique private int renourisheddelight$clipX;
    @Unique private int renourisheddelight$clipY;

    @Inject(method = "renderHearts", at = @At("HEAD"))
    private void renourisheddelight$renderHearts(GuiGraphics guiGraphics, Player player, int left, int top, int rowHeight, int regenIndex, float maxHealth, int health, int displayHealth, int absorptionAmount, boolean highlight, CallbackInfo callback) {
        renourisheddelight$clipActive = false;

        if (ClientConfiguration.getInstance().clipOddMaxHealthHeart && Mth.ceil(maxHealth) % 2 != 0) {
            int index = Mth.ceil((double) maxHealth / 2.0) - 1;
            int row = index / 10;
            int col = index % 10;

            renourisheddelight$clipX = left + col * 8;
            renourisheddelight$clipY = top - row * rowHeight;
            renourisheddelight$clipActive = true;
        }
    }

    @Inject(method = "renderHeart", at = @At("HEAD"))
    private void renourisheddelight$renderHeartStart(GuiGraphics guiGraphics, @Coerce Object heartType, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo callback) {
        if (renourisheddelight$matchesClipTarget(x, y)) {
            guiGraphics.enableScissor(x, y, x + (HEART_SIZE + 1) / 2, y + HEART_SIZE);
        }
    }

    @Inject(method = "renderHeart", at = @At("RETURN"))
    private void renourisheddelight$renderHeartEnd(GuiGraphics guiGraphics, @Coerce Object heartType, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo callback) {
        if (renourisheddelight$matchesClipTarget(x, y)) {
            guiGraphics.disableScissor();
            int edge = x + HEART_SIZE / 2;
            guiGraphics.fill(edge + 1, y + 2, edge + 2, y + HEART_SIZE - 1, blinking ? 0xFFFFFFFF : 0xFF000000);
        }
    }

    @Unique
    private boolean renourisheddelight$matchesClipTarget(int x, int y) {
        return renourisheddelight$clipActive && x == renourisheddelight$clipX && y >= renourisheddelight$clipY - 2 && y <= renourisheddelight$clipY + 1;
    }
}
