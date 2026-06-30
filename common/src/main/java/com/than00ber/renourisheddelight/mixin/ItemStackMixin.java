package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.ConsumableFood;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void renourisheddelight$appendFoodTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        FoodProperties properties = stack.get(DataComponents.FOOD);

        if (properties != null) {
            ConsumableFoodInstance instance = new ConsumableFood(properties).create(stack.getItem());
            List<Component> tooltip = new ArrayList<>(callback.getReturnValue());

            String fed = StringUtil.formatTickDuration(instance.duration, 20);
            tooltip.add(Component.translatable("tooltip.fed", fed).withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.eaten").withStyle(ChatFormatting.DARK_PURPLE));

            String amount = String.valueOf(instance.hearts.amount());
            Component description = Component.translatable(Attributes.MAX_HEALTH.value().getDescriptionId());
            String key = "attribute.modifier.plus." + instance.hearts.operation().id();
            tooltip.add(Component.translatable(key, amount, description).withStyle(ChatFormatting.BLUE));

            callback.setReturnValue(tooltip);
        }
    }
}
