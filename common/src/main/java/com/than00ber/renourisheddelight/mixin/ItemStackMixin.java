package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.food.AttributeModifierInstance;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
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

        if (properties != null || CommonConfiguration.getInstance().hasConfiguredEntry(stack.getItem())) {
            ConsumableFoodInstance instance = ConsumableFoodInstance.create(stack.getItem(), properties, Minecraft.getInstance().getSingleplayerServer());
            List<Component> tooltip = new ArrayList<>(callback.getReturnValue());
            tooltip.add(Component.translatable("tooltip.eaten").withStyle(ChatFormatting.DARK_PURPLE));

            for (AttributeModifierInstance bonus : instance.attributes()) {
                double rawAmount = bonus.modifier().amount();
                String amount = String.valueOf(Math.abs(rawAmount));
                Component description = Component.translatable(bonus.attribute().value().getDescriptionId());
                String key = "attribute.modifier." + (rawAmount >= 0 ? "plus" : "take") + "." + bonus.modifier().operation().id();
                ChatFormatting color = rawAmount >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED;
                String time = StringUtil.formatTickDuration(bonus.duration(), 20);
                MutableComponent duration = Component.literal(" (" + time + ")");
                tooltip.add(Component.literal(" ").append(Component.translatable(key, amount, description)).append(duration).withStyle(color));
            }
            callback.setReturnValue(tooltip);
        }
    }
}
