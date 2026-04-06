package com.than00ber.renourisheddelight.fabric.mixin.client;

import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow protected abstract int getVehicleMaxHearts(LivingEntity entity);

    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int renderPlayerHealth(Gui gui, LivingEntity entity) {
        int hearts = getVehicleMaxHearts(entity);
        return hearts > 0 ? hearts : -1;
    }

    @ModifyVariable(method = "renderPlayerHealth", at = @At("STORE"), name = "t", ordinal = 10)
    private int renderPlayerHealth(int t) {
        return Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty() ? t - 5 : t;
    }
}
