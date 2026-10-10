package com.than00ber.renourisheddelight.fabric.mixin.client;

import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow protected abstract int getVehicleMaxHearts(LivingEntity entity);

    // 1.20.1 draws the food row inline and skips it whenever vehicle hearts are non-zero
    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int renourisheddelight$hideFood(Gui gui, LivingEntity entity) {
        int hearts = getVehicleMaxHearts(entity);
        return hearts > 0 ? hearts : -1;
    }

    // local 18 holds the air bubble row once vehicle rows are subtracted from it
    @ModifyVariable(method = "renderPlayerHealth", at = @At(value = "STORE", ordinal = 1), index = 18)
    private int renourisheddelight$airBubbleRow(int y) {
        return Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty() ? y : y + 10;
    }

    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(F)I", ordinal = 2))
    private int renourisheddelight$heartRows(float rows) {
        return ClientConfiguration.getInstance().compactHealthBar ? 1 : Mth.ceil(rows);
    }
}
