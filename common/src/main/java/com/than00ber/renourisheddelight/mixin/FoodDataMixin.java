package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Unique @Nullable private ServerPlayer renourisheddelight$player;

    @Inject(method = "needsFood", at = @At("HEAD"), cancellable = true)
    public void renourisheddelight$needsFood(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(true);
    }

    @Inject(method = "add", at = @At("HEAD"))
    public void renourisheddelight$add(int nutrition, float saturation, CallbackInfo callback) {
        ServerPlayer player = renourisheddelight$player;

        if (player instanceof DietHolder holder
                && !player.getMainHandItem().has(DataComponents.FOOD)
                && !player.getOffhandItem().has(DataComponents.FOOD)
                && player.pick(5.0D, 0.0F, false) instanceof BlockHitResult result
                && result.getType() == HitResult.Type.BLOCK) {
            Item item = player.level().getBlockState(result.getBlockPos()).getBlock().asItem();
            holder.getDiet().eat(player, item);
            holder.updateDiet();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void renourisheddelight$tick(Player player, CallbackInfo callback) {
        if (renourisheddelight$player == null && player instanceof ServerPlayer serverPlayer) {
            renourisheddelight$player = serverPlayer;
        }
        callback.cancel();
    }
}
