package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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

    private static final double MIN_REGEN_SCALE = 0.5;

    private final List<ConsumableFoodInstance> slots = new ArrayList<>();
    private int ticksSinceDamage = Integer.MAX_VALUE;
    private int regen;
    private int drainCheck;

    public List<ConsumableFoodInstance> getSlots() {
        return slots;
    }

    public void onDamaged() {
        ticksSinceDamage = 0;
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
                .filter(x -> x.item() == item)
                .findFirst()
                .orElse(null);
        boolean replenishable = existing != null 
                && existing.duration() > 0 
                && existing.time() * 100 / existing.duration() > replenishThreshold;

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

        for (AttributeModifierInstance bonus : instance.attributes()) {
            AttributeInstance attribute = player.getAttribute(bonus.attribute());
            
            if (attribute != null) {
                attribute.addPermanentModifier(bonus.modifier());
                
                if (attribute == player.getAttribute(Attributes.MAX_HEALTH)) {
                    ticksSinceDamage = player.level().getGameRules().getInt(GameRuleRegistry.REGEN_DELAY_AFTER_DAMAGE);
                }
            }
        }
    }

    public void removeFromSlot(ServerPlayer player, ConsumableFoodInstance instance) {
        slots.remove(instance);

        for (AttributeModifierInstance bonus : instance.attributes()) {
            AttributeInstance attribute = player.getAttribute(bonus.attribute());
            if (attribute != null) attribute.removeModifier(bonus.modifier());
        }
    }

    private void expireBonuses(ServerPlayer player, ConsumableFoodInstance instance) {
        for (int i = 0; i < instance.attributes().size(); i++) {
            AttributeModifierInstance bonus = instance.attributes().get(i);

            if (bonus.isExpired()) {
                AttributeInstance attribute = player.getAttribute(bonus.attribute());

                if (attribute != null) {
                    if (bonus.attribute().value() == Attributes.MAX_HEALTH.value()) {
                        NetworkManager.sendToPlayer(player, new SuppressHurtFlashPayload());
                    }
                    attribute.removeModifier(bonus.modifier());
                }
                instance.attributes().remove(i);
                i--;
            }
        }
    }

    public boolean drain(ServerPlayer player, int ticks) {
        if (ticks <= 0 || slots.isEmpty()) return false;
        boolean changed = false;

        for (int i = slots.size() - 1; i >= 0; i--) {
            ConsumableFoodInstance instance = slots.get(i);
            instance.tick(ticks);
            expireBonuses(player, instance);

            if (instance.isExpired()) {
                slots.remove(i);
                changed = true;
            }
        }
        return changed;
    }

    public boolean tick(ServerPlayer player) {
        if (player.gameMode.isSurvival()) {
            boolean changed = false;
            GameRules rules = player.level().getGameRules();
            Set<Item> ticked = new HashSet<>();
            boolean nourished = player.hasEffect(EffectRegistry.NOURISHMENT);

            ticksSinceDamage++;
            boolean pastDamageDelay = nourished || ticksSinceDamage >= rules.getInt(GameRuleRegistry.REGEN_DELAY_AFTER_DAMAGE);
            boolean emptyStomach = rules.getBoolean(GameRuleRegistry.DISABLE_HEALTH_REGEN_WHEN_HUNGRY) && slots.isEmpty();
            boolean needsRegen = rules.getBoolean(GameRules.RULE_NATURAL_REGENERATION) && player.isHurt() && pastDamageDelay;
            if (!emptyStomach && needsRegen) regen++;

            if (needsRegen && regen >= computeRegenInterval(rules, nourished)) {
                player.heal(1.0F);
                regen = 0;
                changed = true;

                if (!slots.isEmpty()) {
                    ConsumableFoodInstance instance = slots.stream()
                            .max(Comparator.comparingInt(x -> x.duration() - x.time()))
                            .orElse(null);
                    instance.tick(rules.getInt(GameRuleRegistry.REGEN_HEALTH_FOOD_DRAIN));
                }
            }
            boolean hunger = !nourished && player.hasEffect(MobEffects.HUNGER);
            int extraDrain = 0;
            drainCheck++;

            if (drainCheck >= 20) {
                if (hunger) {
                    extraDrain += rules.getInt(GameRuleRegistry.HUNGER_FOOD_DRAIN);
                }
                if (player.isSprinting()) {
                    extraDrain += rules.getInt(GameRuleRegistry.SPRINT_FOOD_DRAIN);
                }
                drainCheck = 0;
            }
            for (int i = slots.size() - 1; i >= 0; i--) {
                ConsumableFoodInstance instance = slots.get(i);

                if (rules.getBoolean(GameRuleRegistry.FOOD_ITEM_STACKS) || !ticked.contains(instance.item())) {
                    ticked.add(instance.item());
                    instance.tick(1 + extraDrain);
                    changed = true;
                }
                expireBonuses(player, instance);

                if (instance.isExpired()) {
                    slots.remove(i);
                }
            }
            return changed;
        }
        return false;
    }

    private int computeRegenInterval(GameRules rules, boolean nourished) {
        if (nourished) return rules.getInt(GameRuleRegistry.NOURISHMENT_REGEN_TICK_INTERVAL);
        int base = rules.getInt(GameRuleRegistry.REGEN_HEALTH_TICK_INTERVAL);
        if (slots.isEmpty()) return base;
        double avgSaturation = slots.stream()
                .mapToDouble(x -> Optional.ofNullable(x.item().components().get(DataComponents.FOOD))
                        .map(FoodProperties::saturation)
                        .orElse(0.0F))
                .average()
                .orElse(0.0F);
        double scale = Math.max(MIN_REGEN_SCALE, 1.0 / (1.0 + avgSaturation * 0.08));
        return Math.max(5, (int) Math.round(base * scale));
    }

    public static CompoundTag save(Diet diet) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag list = new ListTag();
        diet.slots.forEach(x -> list.add(ConsumableFoodInstance.save(x)));
        compoundTag.put("Slots", list);
        compoundTag.putInt("TicksSinceDamage", diet.ticksSinceDamage);
        compoundTag.putInt("Regen", diet.regen);
        compoundTag.putInt("DrainCheck", diet.drainCheck);
        return compoundTag;
    }

    public static Diet load(CompoundTag compoundTag) {
        Diet diet = new Diet();
        ListTag list = compoundTag.getList("Slots", Tag.TAG_COMPOUND);
        diet.ticksSinceDamage = compoundTag.getInt("TicksSinceDamage");
        diet.regen = compoundTag.getInt("Regen");
        diet.drainCheck = compoundTag.getInt("DrainCheck");
        list.forEach(x -> diet.slots.add(ConsumableFoodInstance.load((CompoundTag) x)));
        return diet;
    }
}