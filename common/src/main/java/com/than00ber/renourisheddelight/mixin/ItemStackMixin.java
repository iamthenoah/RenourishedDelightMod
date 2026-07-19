package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.food.EatingOutcome;
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
    private void finishUsingItem(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> callback) {
        ItemStack stack = (ItemStack) (Object) this;

        if (entity instanceof ServerPlayer player && player instanceof DietHolder holder && stack.get(DataComponents.FOOD) != null) {
            Diet diet = holder.getDiet();
            EatingOutcome outcome = diet.toOutcome(player, stack.getItem());

            if (outcome.isSuccess()) {
                outcome.consume(player, diet, stack.getItem());
                holder.updateDiet();
            }
        }
    }
}
