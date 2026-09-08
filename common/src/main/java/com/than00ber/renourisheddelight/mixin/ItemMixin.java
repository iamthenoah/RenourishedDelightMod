package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.food.EatingOutcome;
import dev.architectury.extensions.injected.InjectedItemExtension;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin implements FeatureElement, ItemLike, InjectedItemExtension {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void renourisheddelight$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.has(DataComponents.FOOD) || !(player instanceof DietHolder holder)) return;

        if (!(player instanceof ServerPlayer serverPlayer)) {
            callback.setReturnValue(InteractionResultHolder.pass(stack));
            return;
        }
        EatingOutcome outcome = holder.getDiet().toOutcome(serverPlayer, stack.getItem());
        outcome.message().ifPresent(x -> player.displayClientMessage(x, true));

        if (outcome.isSuccess()) {
            player.startUsingItem(hand);
            callback.setReturnValue(InteractionResultHolder.consume(stack));
        } else {
            callback.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }
}
