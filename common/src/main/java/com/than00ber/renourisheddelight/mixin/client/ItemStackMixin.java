package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.food.AttributeModifierInstance;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Unique private static final int NUTRITION_BARS = 10;
    @Unique private static final float NUTRITION_HUE_RANGE = 1.0F / 3.0F;
    @Unique private static final String[] NUTRITION_TIERS = { "tasteless", "bland", "fulfilling", "nourishing" };

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void renourisheddelight$getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!(Minecraft.getInstance().getConnection() instanceof FoodConfigHolder holder)) return;
        FoodConfig config = holder.getFoodConfig();
        if (!stack.has(DataComponents.FOOD) && config.entry(stack.getItem()) == null) return;

        Diet diet = Minecraft.getInstance().player instanceof DietHolder owner ? owner.getDiet() : null;
        int decay = diet != null ? diet.nutritionDecay(stack.getItem()) : 0;
        int nutrition = Diet.FULL_NUTRITION - decay;

        ConsumableFoodInstance instance = ConsumableFoodInstance.create(stack.getItem(), config, decay);
        if (instance.attributes().isEmpty()) return;

        List<Component> tooltip = new ArrayList<>(callback.getReturnValue());
        tooltip.add(Component.translatable("tooltip.eaten").withStyle(ChatFormatting.DARK_PURPLE));
        if (diet != null && diet.isDecaying()) tooltip.add(renourisheddelight$nutritionBar(nutrition));

        for (AttributeModifierInstance bonus : instance.attributes()) {
            AttributeModifier.Operation operation = bonus.modifier().operation();
            boolean percent = operation == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || operation == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            double display = percent ? bonus.modifier().amount() * 100.0 : bonus.modifier().amount();
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

    @Unique
    private static MutableComponent renourisheddelight$nutritionBar(int nutrition) {
        int filled = Mth.clamp(Math.round((float) nutrition * NUTRITION_BARS / Diet.FULL_NUTRITION), 0, NUTRITION_BARS);
        int color = Mth.hsvToRgb(nutrition * NUTRITION_HUE_RANGE / Diet.FULL_NUTRITION, 1.0F, 1.0F);
        MutableComponent bar = Component.literal(" ").append(Component.literal("|".repeat(filled)).withStyle(style -> style.withColor(color)));

        if (filled < NUTRITION_BARS) {
            bar.append(Component.literal("|".repeat(NUTRITION_BARS - filled)).withStyle(ChatFormatting.DARK_GRAY));
        }
        String tier = NUTRITION_TIERS[Mth.clamp(nutrition * NUTRITION_TIERS.length / Diet.FULL_NUTRITION, 0, NUTRITION_TIERS.length - 1)];
        return bar.append(Component.literal(" ").append(Component.translatable("tooltip.nutrition." + tier)).withStyle(style -> style.withColor(color)));
    }
}
