package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.ConsumableFood;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.food.EatingOutcome;
import dev.architectury.extensions.injected.InjectedItemExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin implements FeatureElement, ItemLike, InjectedItemExtension {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem().components().has(DataComponents.FOOD) && !level.isClientSide() && player instanceof DietHolder holder) {
            EatingOutcome outcome = holder.getDiet().toOutcome((ServerPlayer) player, stack.getItem());
            outcome.message().ifPresent(x -> player.displayClientMessage(x, true));

            if (outcome.isSuccess()) {
                player.startUsingItem(hand);
                callback.setReturnValue(InteractionResultHolder.consume(stack));
            } else {
                callback.setReturnValue(InteractionResultHolder.fail(stack));
            }
        } else {
            callback.setReturnValue(InteractionResultHolder.pass(player.getItemInHand(hand)));
        }
    }

    @Inject(method = "appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"))
    private void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo callback) {
        FoodProperties properties = stack.get(DataComponents.FOOD);
        if (properties != null) {
            ConsumableFoodInstance instance = new ConsumableFood(properties).create(stack.getItem());

            String fed = StringUtil.formatTickDuration(instance.duration, 20);
            tooltip.add(Component.translatable("tooltip.fed", fed).withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.eaten").withStyle(ChatFormatting.DARK_PURPLE));

            String amount = String.format("%.2f", instance.hearts.amount());
            Component description = Component.translatable(Attributes.MAX_HEALTH.getRegisteredName());
            String key = "attribute.modifier.plus." + instance.hearts.operation().getSerializedName();
            tooltip.add(Component.translatable(key, amount, description).withStyle(ChatFormatting.BLUE));
        }
    }
}