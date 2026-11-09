package com.than00ber.renourisheddelight.fabric.mixin.client;

import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.client.overlay.HealthBarCompat;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Unique private static final int FOOD_RIGHT_OFFSET = 91;
    @Unique private static final int FOOD_BOTTOM_OFFSET = 39;

    @Unique private GuiGraphics renourisheddelight$graphics;

    @Shadow protected abstract int getVehicleMaxHearts(LivingEntity entity);

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void renourisheddelight$captureGraphics(GuiGraphics graphics, CallbackInfo callback) {
        renourisheddelight$graphics = graphics;
    }

    // 1.20.1 draws the food row inline and only when vehicle hearts are zero, so the food bar is drawn
    // in its place here (keeping any pose offset applied by other mods) and the vanilla row is skipped
    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int renourisheddelight$hideFood(Gui gui, LivingEntity entity) {
        int hearts = getVehicleMaxHearts(entity);

        if (hearts == 0 && renourisheddelight$graphics != null) {
            GuiGraphics graphics = renourisheddelight$graphics;
            FoodBarOverlay.render(graphics, graphics.guiWidth() / 2 + FOOD_RIGHT_OFFSET, graphics.guiHeight() - FOOD_BOTTOM_OFFSET);
        }
        return hearts > 0 ? hearts : -1;
    }

    // local 18 holds the air bubble row once vehicle rows are subtracted from it
    @ModifyVariable(method = "renderPlayerHealth", at = @At(value = "STORE", ordinal = 1), index = 18)
    private int renourisheddelight$airBubbleRow(int y) {
        return Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty() ? y : y + 10;
    }

    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(F)I", ordinal = 2))
    private int renourisheddelight$heartRows(float rows) {
        return HealthBarCompat.compactHealthBar() ? 1 : Mth.ceil(rows);
    }
}
