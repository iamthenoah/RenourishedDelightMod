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
public abstract class GuiHeartMixin {

    @Unique private static final int HEART_SIZE = 9;

    @Unique private boolean renourisheddelight$clipActive;
    @Unique private int renourisheddelight$clipX;
    @Unique private int renourisheddelight$clipY;

    @Inject(method = "renderHearts", at = @At("HEAD"))
    private void renourisheddelight$captureOddMaxHealthHeart(GuiGraphics guiGraphics, Player player, int left, int top, int rowHeight, int regenIndex, float maxHealth, int health, int displayHealth, int absorptionAmount, boolean highlight, CallbackInfo callback) {
        renourisheddelight$clipActive = false;

        if (ClientConfiguration.getInstance().clipOddMaxHealthHeart) {
            int maxHealthInt = Mth.ceil(maxHealth);
            
            if (maxHealthInt % 2 != 0 && health >= maxHealthInt) {
                int heartsCount = Mth.ceil((double) maxHealth / 2.0);
                int index = heartsCount - 1;
                int row = index / 10;
                int col = index % 10;

                renourisheddelight$clipX = left + col * 8;
                renourisheddelight$clipY = top - row * rowHeight;
                renourisheddelight$clipActive = true;
            }
        }
    }

    @Inject(method = "renderHeart", at = @At("HEAD"))
    private void renourisheddelight$clipHeartStart(GuiGraphics guiGraphics, @Coerce Object heartType, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo callback) {
        if (renourisheddelight$clipActive && x == renourisheddelight$clipX && y == renourisheddelight$clipY) {
            guiGraphics.enableScissor(x, y, x + (HEART_SIZE + 1) / 2, y + HEART_SIZE);
        }
    }

    @Inject(method = "renderHeart", at = @At("RETURN"))
    private void renourisheddelight$clipHeartEnd(GuiGraphics guiGraphics, @Coerce Object heartType, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo callback) {
        if (renourisheddelight$clipActive && x == renourisheddelight$clipX && y == renourisheddelight$clipY) {
            guiGraphics.disableScissor();
            int edge = x + (HEART_SIZE + 1) / 2;
            guiGraphics.fill(edge - 1, y + 2, edge, y + HEART_SIZE, 0xFF000000);
        }
    }
}
