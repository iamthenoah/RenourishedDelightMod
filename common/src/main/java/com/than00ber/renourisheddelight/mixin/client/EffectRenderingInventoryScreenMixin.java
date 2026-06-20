package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;
import java.util.LinkedHashSet;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin {

    @ModifyVariable(method = "renderEffects", at = @At("STORE"), name = "collection")
    private Collection<MobEffectInstance> modifyActiveEffects(Collection<MobEffectInstance> collection) {
        if (net.minecraft.client.Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty()) {
            int totalRemaining = holder.getDiet().getSlots().stream()
                    .mapToInt(slot -> slot.duration - slot.time)
                    .sum();

            if (totalRemaining > 0) {
                Collection<MobEffectInstance> updated = new LinkedHashSet<>(collection);
                updated.removeIf(x -> x.getEffect().value() == EffectRegistry.FED.value());
                updated.add(new MobEffectInstance(EffectRegistry.FED, totalRemaining, 0, false, false, true));
                return updated;
            }
        }
        return collection;
    }
}