package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.food.EatingOutcome;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Unique @Nullable private ServerPlayer player;

    @Inject(method = "eat(IF)V", at = @At("HEAD"), cancellable = true)
    public void eat(int nutrition, float saturation, CallbackInfo callback) {
        if (player instanceof DietHolder holder && player.pick(5.0D, 0.0F, false) instanceof BlockHitResult result) {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockState state = player.level().getBlockState(result.getBlockPos());
                Item item = state.getBlock().asItem();
                EatingOutcome outcome = holder.getDiet().toOutcome(player, item);
                outcome.message().ifPresent(x -> player.displayClientMessage(x, true));

                if (outcome.isSuccess()) {
                    outcome.consume(player, holder.getDiet(), item);
                } else {
                    callback.cancel();
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(Player player, CallbackInfo callback) {
        if (this.player == null && player instanceof ServerPlayer serverPlayer) {
            this.player = serverPlayer;
        }
        callback.cancel(); // we don't do that here anymore sir
    }
}