package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
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
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, Diet> codec() {
            return StreamCodec.of(this::write, this::read);
        }

        @Override
        public @NotNull Diet copy(@NotNull Diet value) {
            return Diet.load(Diet.save(value));
        }

        private void write(@NotNull RegistryFriendlyByteBuf buffer, @NotNull Diet value) {
            buffer.writeNbt(Diet.save(value));
        }

        private @NotNull Diet read(@NotNull RegistryFriendlyByteBuf buffer) {
            return Diet.load(Objects.requireNonNull(buffer.readNbt()));
        }
    };

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

        FoodProperties properties = item.components().get(DataComponents.FOOD);
        boolean hasEffect = properties != null && !properties.effects().isEmpty();

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
        player.heal((float) (instance.hearts.amount() / 2.0F));
    }

    public void removeFromSlot(ServerPlayer player, ConsumableFoodInstance instance) {
        slots.remove(instance);
        Optional.ofNullable(player.getAttribute(Attributes.MAX_HEALTH)).ifPresent(x -> x.removeModifier(instance.hearts));
    }

    public boolean tick(ServerPlayer player) {
        if (player.gameMode.isSurvival()) {
            boolean changed = false;
            GameRules rules = player.level().getGameRules();
            Set<Item> ticked = new HashSet<>();
            boolean nourished = player.hasEffect(EffectRegistry.NOURISHMENT);
            boolean needsRegen = rules.getBoolean(GameRules.RULE_NATURAL_REGENERATION) && player.isHurt();
            if (needsRegen) regen++;

            if (needsRegen && regen >= computeRegenInterval(rules, nourished)) {
                player.heal(1.0F);
                regen = 0;
                changed = true;

                if (!slots.isEmpty()) {
                    ConsumableFoodInstance instance = slots.stream()
                            .max(Comparator.comparingInt(x -> x.duration - x.time))
                            .orElse(null);
                    instance.time += rules.getInt(GameRuleRegistry.REGEN_HEALTH_FOOD_DRAIN);
                }
            }
            for (int i = slots.size() - 1; i >= 0; i--) {
                ConsumableFoodInstance instance = slots.get(i);

                if (rules.getBoolean(GameRuleRegistry.FOOD_ITEM_STACKS) || !ticked.contains(instance.item)) {
                    ticked.add(instance.item);
                    boolean hunger = !nourished && player.hasEffect(MobEffects.HUNGER);
                    instance.time += hunger ? rules.getInt(GameRuleRegistry.HUNGER_FOOD_DRAIN) : 1;
                    changed = true;
                }
                if (instance.time >= instance.duration) {
                    removeFromSlot(player, instance);
                }
            }
            return changed;
        }
        return false;
    }

    private int computeRegenInterval(GameRules rules, boolean nourished) {
        if (nourished) return 5;
        int base = rules.getInt(GameRuleRegistry.REGEN_HEALTH_TICK_INTERVAL);
        if (slots.isEmpty()) return base;
        double avgSaturation = slots.stream().mapToDouble(x -> Optional
                        .ofNullable(x.item.components().get(DataComponents.FOOD))
                        .map(FoodProperties::saturation)
                        .orElse(0.0F))
                .average()
                .orElse(0.0);
        double scale = 1.0 / (1.0 + avgSaturation * 0.08);
        double multiplier = Configuration.Common.getInstance().regenIntervalMultiplier;
        return Math.max(5, (int) Math.round(base * scale * multiplier));
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
        list.forEach(x -> diet.slots.add(ConsumableFoodInstance.load((CompoundTag) x)));
        return diet;
    }
}