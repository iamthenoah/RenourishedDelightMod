package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.client.KeyBindings;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.food.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    // same block characters as Create's stress and capacity bars
    @Unique private static final String NUTRITION_FILLED = "\u2588";
    @Unique private static final String NUTRITION_EMPTY = "\u2592";
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
        boolean decaying = diet != null && diet.isDecaying();
        int nutritionColor = renourisheddelight$nutritionColor(nutrition);

        for (AttributeModifierInstance bonus : instance.attributes()) {
            AttributeModifier.Operation operation = bonus.modifier().operation();
            boolean percent = operation == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || operation == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            double display = percent ? bonus.modifier().amount() * 100.0 : bonus.modifier().amount();
            String key = "attribute.modifier." + (display >= 0 ? "plus" : "take") + "." + operation.id();
            String amount = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(display));
            Component description = Component.translatable(bonus.attribute().value().getDescriptionId());
            MutableComponent duration = Component.literal(" (" + StringUtil.formatTickDuration(bonus.duration(), 20) + ")");
            if (decaying) duration.withStyle(style -> style.withColor(nutritionColor));
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable(key, amount, description))
                    .append(duration)
                    .withStyle(display >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED));
        }
        boolean restricted = diet != null && diet.isRestricted();

        if (decaying || restricted) {
            if (!ClientConfiguration.getInstance().holdToViewNutrition || KeyBindings.isDown(KeyBindings.VIEW_NUTRITION)) {
                if (decaying) tooltip.add(renourisheddelight$nutritionBar(nutrition));

                if (restricted) {
                    MutableComponent restrictions = renourisheddelight$restrictions(stack.getItem(), diet);
                    if (restrictions != null) tooltip.add(restrictions);
                    tooltip.add(renourisheddelight$status(stack.getItem(), diet));
                }
            } else {
                Component keybind = KeyBindings.VIEW_NUTRITION.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.YELLOW);
                tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.view_nutrition", keybind)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        callback.setReturnValue(tooltip);
    }

    @Unique
    private static Component renourisheddelight$status(Item item, Diet diet) {
        DietRestrictions lookup = DietRestrictions.client();
        EatingOutcome outcome = EatingOutcome.outcome(diet, item, lookup);
        ConsumableFoodInstance target = outcome.consume(diet, item, lookup);
        Component status = outcome.getMessage(target != null ? target.item().getDescription() : Component.empty());
        return Component.literal(" ").append(status);
    }

    @Unique
    private static @Nullable MutableComponent renourisheddelight$restrictions(Item item, Diet diet) {
        DietRestrictions lookup = DietRestrictions.client();
        Set<ResourceLocation> restrictions = lookup.get(item);
        if (restrictions.isEmpty()) return null;

        Set<ResourceLocation> active = diet.getSlots().stream()
                .filter(x -> x.item() != item)
                .flatMap(x -> lookup.get(x.item()).stream())
                .collect(Collectors.toSet());
        ChatFormatting clash = EatingOutcome.outcome(diet, item, lookup) == EatingOutcome.REPLACE_RESTRICTED ? ChatFormatting.GOLD : ChatFormatting.RED;
        MutableComponent line = Component.literal(" ").withStyle(ChatFormatting.GRAY);
        List<ResourceLocation> sorted = restrictions.stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList();

        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) line.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            ResourceLocation restriction = sorted.get(i);
            line.append(DietRestrictions.name(restriction).withStyle(active.contains(restriction) ? clash : ChatFormatting.GRAY));
        }
        return line;
    }

    @Unique
    private static int renourisheddelight$nutritionColor(int nutrition) {
        return Mth.hsvToRgb(nutrition * NUTRITION_HUE_RANGE / Diet.FULL_NUTRITION, 1.0F, 1.0F);
    }

    @Unique
    private static MutableComponent renourisheddelight$nutritionBar(int nutrition) {
        // one square per tier, so the fill always matches the tier name next to it
        int tier = Mth.clamp(nutrition * NUTRITION_TIERS.length / Diet.FULL_NUTRITION, 0, NUTRITION_TIERS.length - 1);
        int filled = tier + 1;
        int color = renourisheddelight$nutritionColor(nutrition);
        MutableComponent bar = Component.literal(" ").append(Component.literal(NUTRITION_FILLED.repeat(filled)).withStyle(style -> style.withColor(color)));

        if (filled < NUTRITION_TIERS.length) {
            bar.append(Component.literal(NUTRITION_EMPTY.repeat(NUTRITION_TIERS.length - filled)).withStyle(ChatFormatting.DARK_GRAY));
        }
        return bar.append(Component.literal(" ").append(Component.translatable("tooltip.nutrition." + NUTRITION_TIERS[tier])).withStyle(ChatFormatting.WHITE));
    }
}
