package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.StarvationEntry;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Diet {

    public static final int SLEEP_DRAIN = 12000;
    public static final int FULL_NUTRITION = 100;

    private static final int HUNGER_DRAIN_PER_SECOND = 2;
    private static final int REGEN_DRAIN = 3;
    private static final int NOURISHED_REGEN_SPEEDUP = 3;
    private static final int STARVING_MESSAGE_INTERVAL = 40;

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
    private final Map<Item, Integer> depletion = new LinkedHashMap<>();
    private int ticksSinceDamage = Integer.MAX_VALUE;
    private int regen;
    private int drainTimer;
    private int drainRemainder;
    private int starving;
    private boolean decaying = true;
    private int capacity;

    public List<ConsumableFoodInstance> getSlots() {
        return slots;
    }

    public void onDamaged() {
        ticksSinceDamage = 0;
    }

    public boolean isDecaying() {
        return decaying;
    }

    public int getCapacity() {
        return capacity;
    }

    public int nutritionDecay(Item item) {
        return decaying ? depletion.getOrDefault(item, 0) : 0;
    }

    private ConsumableFoodInstance eat(ServerPlayer player, Item item, FoodConfig config) {
        ConsumableFoodInstance instance = ConsumableFoodInstance.create(item, config, nutritionDecay(item));
        decay(player, item);
        return instance;
    }

    private void decay(ServerPlayer player, Item item) {
        GameRules rules = player.level().getGameRules();
        int step = Math.max(0, rules.getInt(GameRuleRegistry.NUTRITION_DECAY_RATE));

        if (rules.getBoolean(GameRuleRegistry.DO_NUTRITION_DECAY) && step > 0) {
            int current = depletion.getOrDefault(item, 0);
            depletion.remove(item);
            int protect = Math.max(0, rules.getInt(GameRuleRegistry.NUTRITION_DECAY_WINDOW) - 1);
            List<Item> tracked = new ArrayList<>(depletion.keySet());

            for (int i = 0; i < tracked.size() - protect; i++) {
                Item stale = tracked.get(i);
                int value = depletion.get(stale) - step;
                if (value > 0) depletion.put(stale, value);
                else depletion.remove(stale);
            }
            depletion.put(item, Math.min(current + step, maxDecay(rules)));
        }
    }

    public boolean resetDecay() {
        if (depletion.isEmpty()) return false;
        depletion.clear();
        return true;
    }

    public boolean resetDecay(Item item) {
        return depletion.remove(item) != null;
    }

    private static int maxDecay(GameRules rules) {
        return FULL_NUTRITION - Mth.clamp(rules.getInt(GameRuleRegistry.NUTRITION_DECAY_FLOOR), 0, FULL_NUTRITION);
    }

    public void consume(ServerPlayer player, Item item) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        FoodConfig config = FoodConfigSavedData.get(server).getFoodConfig();
        GameRules rules = player.level().getGameRules();
        int maxFoods = Math.max(1, rules.getInt(GameRuleRegistry.MAX_ACTIVE_FOODS));
        ConsumableFoodInstance active = slots.stream().filter(x -> x.item() == item).findFirst().orElse(null);

        if (active != null) {
            replaceSlot(player, config, item, active);
        } else if (slots.size() < maxFoods) {
            addToSlot(player, eat(player, item, config));
        } else {
            replaceSlot(player, config, item, slots.stream().min(Comparator.comparingInt(x -> x.duration() - x.time())).orElse(null));
        }
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        boolean harmful = properties != null && properties.effects().stream()
                .anyMatch(x -> x.effect().getEffect().value().getCategory() == MobEffectCategory.HARMFUL);

        if (slots.size() >= maxFoods && !harmful) {
            nourish(player, rules);
        }
    }

    public boolean drain(ServerPlayer player, int amount) {
        if (amount <= 0 || slots.isEmpty()) return false;
        int ticks = scale(player.level().getGameRules(), amount);

        if (ticks > 0) {
            slots.forEach(x -> x.tick(ticks));
        }
        expire(player);
        return true;
    }

    public boolean tick(ServerPlayer player) {
        GameRules rules = player.level().getGameRules();
        boolean enabled = rules.getBoolean(GameRuleRegistry.DO_NUTRITION_DECAY);
        int limit = Math.max(1, rules.getInt(GameRuleRegistry.MAX_ACTIVE_FOODS));
        boolean toggled = decaying != enabled || capacity != limit;
        decaying = enabled;
        capacity = limit;

        if (!player.gameMode.isSurvival()) return toggled;
        boolean nourished = player.hasEffect(EffectRegistry.nourishment());
        ticksSinceDamage++;
        int amount = 1;

        if (++drainTimer >= 20) {
            drainTimer = 0;
            if (!nourished && player.hasEffect(MobEffects.HUNGER)) amount += HUNGER_DRAIN_PER_SECOND;
        }
        boolean changed = toggled | drain(player, amount);
        changed |= regenerate(player, rules, nourished);

        if (slots.isEmpty() && nourished) {
            player.removeEffect(EffectRegistry.nourishment());
        }
        starve(player, rules);
        return changed;
    }

    public void clearModifiers(ServerPlayer player) {
        slots.forEach(instance -> instance.attributes().forEach(bonus -> detach(player, bonus, false)));
    }

    public boolean clear(ServerPlayer player) {
        if (slots.isEmpty()) return false;
        slots.forEach(instance -> instance.attributes().forEach(bonus -> detach(player, bonus, true)));
        slots.clear();
        regen = 0;
        drainTimer = 0;
        drainRemainder = 0;
        starving = 0;
        player.removeEffect(EffectRegistry.nourishment());
        return true;
    }

    private void nourish(ServerPlayer player, GameRules rules) {
        if (rules.getBoolean(GameRuleRegistry.DO_NOURISHMENT)) {
            int percent = rules.getInt(GameRuleRegistry.NOURISHMENT_DURATION_PERCENT);

            if (percent > 0 && !slots.isEmpty()) {
                int shortest = slots.stream().mapToInt(ConsumableFoodInstance::duration).min().orElse(0);
                int duration = (int) Math.round(shortest * (percent / 100.0));

                if (duration > 0) {
                    player.addEffect(new MobEffectInstance(EffectRegistry.nourishment(), duration, 0, false, false, true));
                }
            }
        }
    }

    private boolean regenerate(ServerPlayer player, GameRules rules, boolean nourished) {
        if (!slots.isEmpty() && player.isHurt() && rules.getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            if (nourished || ticksSinceDamage >= rules.getInt(GameRuleRegistry.REGEN_DELAY_AFTER_DAMAGE)) {
                int interval = Math.max(1, rules.getInt(GameRuleRegistry.REGEN_INTERVAL));
                if (nourished) interval = Math.max(1, interval / NOURISHED_REGEN_SPEEDUP);
                if (++regen < interval) return false;

                regen = 0;
                player.heal(1.0F);
                int ticks = scale(rules, REGEN_DRAIN);

                if (ticks > 0) {
                    slots.stream().max(Comparator.comparingInt(x -> x.duration() - x.time())).ifPresent(x -> x.tick(ticks));
                }
                expire(player);
                return true;
            }
        } else {
            regen = 0;
        }
        return false;
    }

    private void starve(ServerPlayer player, GameRules rules) {
        MinecraftServer server = player.getServer();

        if (slots.isEmpty() && server != null && rules.getBoolean(GameRuleRegistry.DO_STARVATION)) {
            starving++;
            List<StarvationEntry> reached = StarvationEntry.reached(FoodConfigSavedData.get(server).getFoodConfig().starvation, starving);
            if (reached.isEmpty()) return;

            if (starving % STARVING_MESSAGE_INTERVAL == 0) {
                player.displayClientMessage(Component.translatable("message.starving").withStyle(ChatFormatting.RED), true);
            }
            for (int i = 0; i < reached.size(); i++) {
                StarvationEntry entry = reached.get(i);
                Holder<MobEffect> effect = StarvationEntry.resolveEffect(entry.effect);

                if (effect != null) {
                    player.addEffect(new MobEffectInstance(effect, 2, entry.levelAt(reached.size() - 1 - i) - 1, true, false, true));
                }
            }
        } else {
            starving = 0;
        }
    }

    private void addToSlot(ServerPlayer player, ConsumableFoodInstance instance) {
        slots.add(instance);
        attach(player, instance);
    }

    private void replaceSlot(ServerPlayer player, FoodConfig config, Item item, @Nullable ConsumableFoodInstance previous) {
        if (previous == null) return;
        int index = slots.indexOf(previous);
        if (index < 0) return;

        previous.attributes().forEach(bonus -> detach(player, bonus, true));
        ConsumableFoodInstance instance = eat(player, item, config);
        slots.set(index, instance);
        attach(player, instance);
    }

    private void attach(ServerPlayer player, ConsumableFoodInstance instance) {
        for (AttributeModifierInstance bonus : instance.attributes()) {
            AttributeInstance attribute = player.getAttribute(bonus.attribute());
            if (attribute == null) continue;
            attribute.addPermanentModifier(bonus.modifier());

            if (bonus.attribute().value() == Attributes.MAX_HEALTH.value()) {
                ticksSinceDamage = player.level().getGameRules().getInt(GameRuleRegistry.REGEN_DELAY_AFTER_DAMAGE);
            }
        }
    }

    private void expire(ServerPlayer player) {
        for (int i = slots.size() - 1; i >= 0; i--) {
            ConsumableFoodInstance instance = slots.get(i);

            for (int j = instance.attributes().size() - 1; j >= 0; j--) {
                AttributeModifierInstance bonus = instance.attributes().get(j);

                if (bonus.isExpired()) {
                    detach(player, bonus, true);
                    instance.attributes().remove(j);
                }
            }
            if (instance.isExpired()) slots.remove(i);
        }
    }

    private void detach(ServerPlayer player, AttributeModifierInstance bonus, boolean notify) {
        AttributeInstance attribute = player.getAttribute(bonus.attribute());
        if (attribute == null) return;

        if (notify && bonus.attribute().value() == Attributes.MAX_HEALTH.value()) {
            NetworkManager.sendToPlayer(player, new SuppressHurtFlashPayload());
        }
        attribute.removeModifier(bonus.modifier());
    }

    private int scale(GameRules rules, int amount) {
        long total = (long) amount * Math.max(0, rules.getInt(GameRuleRegistry.FOOD_DRAIN_RATE)) + drainRemainder;
        drainRemainder = (int) (total % 100L);
        return (int) Math.min(Integer.MAX_VALUE, total / 100L);
    }

    public static CompoundTag save(Diet diet) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag list = new ListTag();
        diet.slots.forEach(x -> list.add(ConsumableFoodInstance.save(x)));
        compoundTag.put("Slots", list);
        compoundTag.putInt("TicksSinceDamage", diet.ticksSinceDamage);
        compoundTag.putInt("Regen", diet.regen);
        compoundTag.putInt("Starving", diet.starving);
        compoundTag.putBoolean("Decaying", diet.decaying);
        compoundTag.putInt("Capacity", diet.capacity);
        ListTag depleted = new ListTag();

        diet.depletion.forEach((item, value) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Item", BuiltInRegistries.ITEM.getKey(item).toString());
            entry.putInt("Value", value);
            depleted.add(entry);
        });
        compoundTag.put("Depletion", depleted);
        return compoundTag;
    }

    public static Diet load(CompoundTag tag) {
        Diet diet = new Diet();
        ListTag list = tag.getList("Slots", Tag.TAG_COMPOUND);
        diet.ticksSinceDamage = tag.getInt("TicksSinceDamage");
        diet.regen = tag.getInt("Regen");
        diet.starving = tag.getInt("Starving");
        diet.decaying = !tag.contains("Decaying") || tag.getBoolean("Decaying");
        diet.capacity = tag.getInt("Capacity");
        list.forEach(x -> diet.slots.add(ConsumableFoodInstance.load((CompoundTag) x)));

        for (Tag entry : tag.getList("Depletion", Tag.TAG_COMPOUND)) {
            CompoundTag stored = (CompoundTag) entry;
            Item item = resolveItem(stored.getString("Item"));
            if (item != null) diet.depletion.put(item, stored.getInt("Value"));
        }
        return diet;
    }

    private static @Nullable Item resolveItem(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            return item != Items.AIR ? item : null;
        } catch (Exception exception) {
            return null;
        }
    }
}
