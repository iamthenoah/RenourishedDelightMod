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

    public EatingOutcome toOutcome(ServerPlayer player, Item item) {
        GameRules rules = player.level().getGameRules();
        boolean allowSameItem = rules.getBoolean(GameRuleRegistry.ALLOW_EATING_SAME_ITEM);
        boolean enoughSpace = slots.size() < rules.getInt(GameRuleRegistry.MAX_CONSUMABLE_FOOD);
        boolean replaceLowest = rules.getBoolean(GameRuleRegistry.REPLACE_LOWEST_FOOD_ITEM);
        int replenishThreshold = 100 - rules.getInt(GameRuleRegistry.FOOD_REPLENISHABLE_THRESHOLD);

        FoodProperties properties = item.getFoodProperties();
        boolean hasEffect = properties != null && !properties.getEffects().isEmpty();

        ConsumableFoodInstance existing = slots.stream()
                .filter(x -> x.item == item)
                .findFirst()
                .orElse(null);
        boolean replenishable = existing != null && existing.time * 100 / existing.duration > replenishThreshold;

        return replenishable
                ? EatingOutcome.REPLENISH
                : allowSameItem || existing == null
                /*  */ ? enoughSpace
                /*      */ ? EatingOutcome.CONSUME
                /*      */ : replaceLowest
                /*          */ ? EatingOutcome.REPLACE_LOW
                /*          */ : hasEffect
                /*              */ ? EatingOutcome.EFFECTS_ONLY
                /*              */ : EatingOutcome.TOO_MANY
                /*  */ : hasEffect
                /*      */ ? EatingOutcome.EFFECTS_ONLY
                /*      */ : enoughSpace
                /*          */ ? EatingOutcome.NOT_BALANCED
                /*          */ : replaceLowest
                /*              */ ? EatingOutcome.REPLACE_LOW
                /*              */ : EatingOutcome.TOO_MANY;
    }

    public void addToSlot(ServerPlayer player, ConsumableFoodInstance instance) {
        slots.add(instance);
        Optional.ofNullable(player.getAttribute(Attributes.MAX_HEALTH)).ifPresent(x -> x.addPermanentModifier(instance.hearts));
        player.heal((float) (instance.hearts.getAmount() / 2.0F));
    }

    public void removeFromSlot(ServerPlayer player, ConsumableFoodInstance instance) {
        slots.remove(instance);
        Optional.ofNullable(player.getAttribute(Attributes.MAX_HEALTH)).ifPresent(x -> x.removeModifier(instance.hearts));
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
                changed = true;
            }
            if (rules.getBoolean(GameRuleRegistry.FOOD_ITEM_STACKS) || !ticked.contains(instance.item)) {
                ticked.add(instance.item);
                instance.time += player.hasEffect(MobEffects.HUNGER) ? rules.getInt(GameRuleRegistry.HUNGER_FOOD_DRAIN) : 1;
                changed = true;
            }
            if (instance.time >= instance.duration) {
                removeFromSlot(player, instance);
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