package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void renourisheddelight$finishUsingItem(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> callback) {
        ItemStack stack = (ItemStack) (Object) this;

        if (entity instanceof ServerPlayer player && player instanceof DietHolder holder && stack.has(DataComponents.FOOD)) {
            holder.getDiet().eat(player, stack.getItem());
            holder.updateDiet();
        }
    }
}
