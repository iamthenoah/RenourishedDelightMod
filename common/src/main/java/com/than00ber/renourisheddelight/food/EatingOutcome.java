package com.than00ber.renourisheddelight.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public enum EatingOutcome {
    CONSUME("tooltip.eating.consume", true),                // takes a free slot
    REPLENISH("tooltip.eating.replenish", true),            // tops up the slot already holding the same item
    REPLACE_RESTRICTED("tooltip.eating.replace", true),     // swaps out the one active food sharing a restriction with it
    REPLACE_LOW("tooltip.eating.consume", true),            // every slot is full, swaps out the food closest to running out
    RESTRICTED("tooltip.eating.blocked", false),            // shares a restriction with an active food that cannot be swapped out
    TOO_FULL("tooltip.eating.blocked", false),              // every slot is full and replacing is disabled
    ALREADY_ACTIVE("tooltip.eating.already_active", false); // already active and top-ups are disabled

    final String key;
    final boolean success;

    EatingOutcome(String key, boolean success) {
        this.key = key;
        this.success = success;
    }

    public Component getMessage(Object ...objects) {
        ChatFormatting color = success ? ChatFormatting.GREEN : ChatFormatting.RED;
        return Component.translatable(key, objects).withStyle(color);
    }

    public static EatingOutcome outcome(Diet diet, Item item, DietRestrictions restrictions) {
        if (diet.getSlots().stream().noneMatch(x -> x.item() == item)) {
            if (diet.isRestricted()) {
                List<ConsumableFoodInstance> conflicts = conflicts(diet, item, restrictions);

                if (conflicts.size() > 1) {
                    return EatingOutcome.RESTRICTED;
                } else if (conflicts.size() == 1) {
                    return diet.isReplacing() ? EatingOutcome.REPLACE_RESTRICTED : EatingOutcome.RESTRICTED;
                }
            }
            return diet.getCapacity() <= 0 || diet.getSlots().size() < diet.getCapacity()
                    ? EatingOutcome.CONSUME : diet.isReplacing()
                    ? EatingOutcome.REPLACE_LOW
                    : EatingOutcome.TOO_FULL;
        }
        return diet.isToppingUp() ? EatingOutcome.REPLENISH : EatingOutcome.ALREADY_ACTIVE;
    }

    public @Nullable ConsumableFoodInstance consume(Diet diet, Item item, DietRestrictions restrictions) {
        return switch (this) {
            case REPLENISH, ALREADY_ACTIVE -> diet.getSlots().stream().filter(x -> x.item() == item).findFirst().orElse(null);
            case REPLACE_RESTRICTED, RESTRICTED -> conflicts(diet, item, restrictions).stream().findFirst().orElse(null);
            case REPLACE_LOW -> diet.getSlots().stream().min(Comparator.comparingInt(x -> x.duration() - x.time())).orElse(null);
            case CONSUME, TOO_FULL -> null;
        };
    }

    private static List<ConsumableFoodInstance> conflicts(Diet diet, Item item, DietRestrictions restrictions) {
        Set<ResourceLocation> eating = restrictions.get(item);

        return eating.isEmpty() ? List.of() : diet.getSlots().stream()
                .filter(x -> restrictions.get(x.item()).stream()
                .anyMatch(eating::contains)).toList();
    }
}
