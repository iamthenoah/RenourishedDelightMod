package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeModifierInstance;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void renourisheddelight$getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> callback) {
        ItemStack stack = (ItemStack) (Object) this;

        if (Minecraft.getInstance().getConnection() instanceof FoodConfigHolder holder && FoodItemEntry.get(holder.getFoodConfig(), stack.getItem()) != null) {
            ConsumableFoodInstance instance = ConsumableFoodInstance.create(stack.getItem(), holder.getFoodConfig());
            List<Component> tooltip = new ArrayList<>(callback.getReturnValue());
            tooltip.add(Component.translatable("tooltip.eaten").withStyle(ChatFormatting.DARK_PURPLE));

            for (AttributeModifierInstance bonus : instance.attributes()) {
                AttributeModifier.Operation operation = bonus.modifier().operation();
                boolean percent = operation == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || operation == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                double rawAmount = bonus.modifier().amount();
                double display = percent ? rawAmount * 100.0 : rawAmount;
                String key = "attribute.modifier." + (display >= 0 ? "plus" : "take") + "." + operation.id();
                String amount = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(display));
                Component description = Component.translatable(bonus.attribute().value().getDescriptionId());
                tooltip.add(Component.literal(" ")
                        .append(Component.translatable(key, amount, description))
                        .append(Component.literal(" (" + StringUtil.formatTickDuration(bonus.duration(), 20) + ")"))
                        .withStyle(display >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED));
            }
            callback.setReturnValue(tooltip);
        }
    }
}
