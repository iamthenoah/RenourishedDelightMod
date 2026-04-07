package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Diet {

    public static final EntityDataSerializer<Diet> DATA_SERIALIZER = new EntityDataSerializer<>() {
        @Override
        public void write(@NotNull FriendlyByteBuf buffer, @NotNull Diet value) {
            buffer.writeNbt(Diet.save(value));
        }

        @Override
        public @NotNull Diet read(FriendlyByteBuf buffer) {
            return Diet.load(Objects.requireNonNull(buffer.readNbt()));
        }

        @Override
        public @NotNull Diet copy(@NotNull Diet value) {
            return Diet.load(Diet.save(value));
        }
    };

    public static void init() {
        EntityDataSerializers.registerSerializer(Diet.DATA_SERIALIZER);
    }

    private final List<ConsumableFoodInstance> slots = new ArrayList<>();
    private int regen;

    public List<ConsumableFoodInstance> getSlots() {
        return slots;
    }

    public EatingOutcome toOutcome(ServerPlayer player, ItemStack stack) {
        GameRules rules = player.level().getGameRules();
        boolean allowSameItem = rules.getBoolean(GameRuleRegistry.ALLOW_EATING_SAME_ITEM);
        boolean replenishable = rules.getBoolean(GameRuleRegistry.ALLOW_FOOD_REPLENISHMENT);
        boolean enoughSpace = slots.size() < rules.getInt(GameRuleRegistry.MAX_CONSUMABLE_FOOD);

        FoodProperties properties = stack.getItem().getFoodProperties();
        boolean hasEffect = properties != null && !properties.getEffects().isEmpty();
        boolean alreadyExists = slots.stream().anyMatch(x -> x.item == stack.getItem());
        return !enoughSpace && (!replenishable || !alreadyExists) ? EatingOutcome.TOO_MANY
                : allowSameItem || !alreadyExists ? EatingOutcome.SUCCESS
                : hasEffect ? EatingOutcome.SUCCESS_EFFECTS_ONLY
                : replenishable ? EatingOutcome.SUCCESS_REPLENISH
                : EatingOutcome.NOT_BALANCED;
    }

    public void addToSlot(ServerPlayer player, ConsumableFoodInstance instance) {
        slots.add(instance);
        Optional.ofNullable(player.getAttribute(Attributes.MAX_HEALTH)).ifPresent(x -> x.addPermanentModifier(instance.hearts));
        player.heal((float) (instance.hearts.getAmount() / 2.0F));
    }

    public boolean tick(ServerPlayer player) {
        boolean changed = false;
        GameRules rules = player.level().getGameRules();
        Set<Item> ticked = new HashSet<>();
        boolean needsRegen = rules.getBoolean(GameRules.RULE_NATURAL_REGENERATION) && player.isHurt();

        if (needsRegen) {
            regen++;
        }
        for (int i = slots.size() - 1; i >= 0; i--) {
            ConsumableFoodInstance instance = slots.get(i);

            if (needsRegen && regen >= rules.getInt(GameRuleRegistry.REGEN_HEALTH_TICK_INTERVAL)) {
                player.heal(1.0F);
                instance.time += rules.getInt(GameRuleRegistry.REGEN_HEALTH_FOOD_DRAIN);
                regen = 0;
            }
            if (rules.getBoolean(GameRuleRegistry.FOOD_ITEM_STACKS) || !ticked.contains(instance.item)) {
                ticked.add(instance.item);
                instance.time += player.hasEffect(MobEffects.HUNGER) ? rules.getInt(GameRuleRegistry.HUNGER_FOOD_DRAIN) : 1;
            }
            if (instance.time >= instance.duration) {
                slots.remove(i);
                Optional.ofNullable(player.getAttribute(Attributes.MAX_HEALTH)).ifPresent(x -> x.removeModifier(instance.hearts));
                changed = true;
            }
        }
        return changed;
    }

    public static CompoundTag save(Diet diet) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag list = new ListTag();
        diet.slots.forEach(x -> list.add(ConsumableFoodInstance.save(x)));
        compoundTag.put("Slots", list);
        return compoundTag;
    }

    public static Diet load(CompoundTag compoundTag) {
        Diet diet = new Diet();
        ListTag list = compoundTag.getList("Slots", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            diet.slots.add(ConsumableFoodInstance.load(list.getCompound(i)));
        }
        return diet;
    }
}
