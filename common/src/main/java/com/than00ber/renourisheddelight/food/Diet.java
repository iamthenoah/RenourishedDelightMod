package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.config.ServerConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.StarvationEntry;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
    private static final int NOURISHMENT_THRESHOLD = 95;

    public static final EntityDataSerializer<Diet> DATA_SERIALIZER = new EntityDataSerializer<>() {
        @Override
        public void write(@NotNull FriendlyByteBuf buffer, @NotNull Diet value) {
            buffer.writeNbt(Diet.save(value));
        }

        @Override
        public @NotNull Diet read(@NotNull FriendlyByteBuf buffer) {
            return Diet.load(Objects.requireNonNull(buffer.readNbt()));
        }

        @Override
        public @NotNull Diet copy(@NotNull Diet value) {
            return Diet.load(Diet.save(value));
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

    private ConsumableFoodInstance eat(Item item, FoodConfig config) {
        ConsumableFoodInstance instance = ConsumableFoodInstance.create(item, config, nutritionDecay(item));
        int step = Math.max(0, ServerConfiguration.getInstance().nutritionDecayRate);

        if (ServerConfiguration.getInstance().doNutritionDecay && step > 0) {
            int current = depletion.getOrDefault(item, 0);
            depletion.remove(item);
            int protect = Math.max(0, ServerConfiguration.getInstance().nutritionDecayWindow - 1);
            List<Item> tracked = new ArrayList<>(depletion.keySet());

            for (int i = 0; i < tracked.size() - protect; i++) {
                Item stale = tracked.get(i);
                int value = depletion.get(stale) - step;
                if (value > 0) depletion.put(stale, value);
                else depletion.remove(stale);
            }
            depletion.put(item, Math.min(current + step, maxDecay()));
        }
        return instance;
    }

    public boolean resetDecay() {
        if (depletion.isEmpty()) return false;
        depletion.clear();
        return true;
    }

    public boolean resetDecay(Item item) {
        return depletion.remove(item) != null;
    }

    private static int maxDecay() {
        return FULL_NUTRITION - Mth.clamp(ServerConfiguration.getInstance().nutritionDecayFloor, 0, FULL_NUTRITION);
    }

    public void consume(ServerPlayer player, Item item) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        FoodConfig config = FoodConfigSavedData.get(server).getFoodConfig();
        int maxFoods = Math.max(1, ServerConfiguration.getInstance().maxConsumableFood);
        ConsumableFoodInstance active = slots.stream().filter(x -> x.item() == item).findFirst().orElse(null);

        if (active != null) {
            replaceSlot(player, config, item, active);
        } else if (slots.size() < maxFoods) {
            addToSlot(player, eat(item, config));
        } else {
            replaceSlot(player, config, item, slots.stream().min(Comparator.comparingInt(x -> x.duration() - x.time())).orElse(null));
        }
        FoodProperties properties = item.getFoodProperties();
        boolean harmful = properties != null && properties.getEffects().stream().anyMatch(x -> x.getFirst().getEffect().getCategory() == MobEffectCategory.HARMFUL);

        if (slots.size() >= maxFoods && !harmful) {
            nourish(player);
        }
    }

    public boolean drain(ServerPlayer player, int amount) {
        if (amount <= 0 || slots.isEmpty()) return false;
        int ticks = scale(amount);
        if (ticks > 0) slots.forEach(x -> x.tick(ticks));
        expire(player);
        return true;
    }

    public boolean tick(ServerPlayer player) {
        boolean enabled = ServerConfiguration.getInstance().doNutritionDecay;
        int limit = Math.max(1, ServerConfiguration.getInstance().maxConsumableFood);
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
        changed |= regenerate(player, nourished);

        if (slots.isEmpty() && nourished) {
            player.removeEffect(EffectRegistry.nourishment());
        }
        starve(player);
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

    private void nourish(ServerPlayer player) {
        if (!ServerConfiguration.getInstance().doNourishment || slots.isEmpty()) return;
        if (!slots.stream().allMatch(Diet::isFresh)) return;
        int duration = slots.stream().mapToInt(ConsumableFoodInstance::duration).min().orElse(0);

        if (duration > 0) {
            player.addEffect(new MobEffectInstance(EffectRegistry.nourishment(), duration, 0, false, false, true));
        }
    }

    private static boolean isFresh(ConsumableFoodInstance instance) {
        return (instance.duration() - instance.time()) * 100 >= instance.duration() * NOURISHMENT_THRESHOLD;
    }

    private boolean regenerate(ServerPlayer player, boolean nourished) {
        GameRules rules = player.level().getGameRules();

        if (!slots.isEmpty() && player.isHurt() && rules.getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            if (nourished || ticksSinceDamage >= ServerConfiguration.getInstance().regenDelayAfterDamage) {
                int interval = Math.max(1, ServerConfiguration.getInstance().regenHealthTickInterval);
                if (nourished) interval = Math.max(1, interval / NOURISHED_REGEN_SPEEDUP);
                if (++regen < interval) return false;

                regen = 0;
                player.heal(1.0F);
                int ticks = scale(REGEN_DRAIN);

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

    private void starve(ServerPlayer player) {
        MinecraftServer server = player.getServer();

        if (slots.isEmpty() && server != null && ServerConfiguration.getInstance().doStarvation) {
            starving++;
            List<StarvationEntry> reached = StarvationEntry.reached(FoodConfigSavedData.get(server).getFoodConfig().starvation, starving);
            if (reached.isEmpty()) return;

            for (int i = 0; i < reached.size(); i++) {
                StarvationEntry entry = reached.get(i);
                if (starving < entry.after) return;
                if (starving == entry.after) player.displayClientMessage(Component.translatable("message.starving").withStyle(ChatFormatting.RED), true);
                MobEffect effect = StarvationEntry.resolveEffect(entry.effect);

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
        ConsumableFoodInstance instance = eat(item, config);
        slots.set(index, instance);
        attach(player, instance);
    }

    private void attach(ServerPlayer player, ConsumableFoodInstance instance) {
        for (AttributeModifierInstance bonus : instance.attributes()) {
            AttributeInstance attribute = player.getAttribute(bonus.attribute());

            if (attribute != null) {
                attribute.addPermanentModifier(bonus.modifier());

                if (bonus.attribute() == Attributes.MAX_HEALTH) {
                    ticksSinceDamage = ServerConfiguration.getInstance().regenDelayAfterDamage;
                }
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
            if (instance.isExpired())
                slots.remove(i);
        }
    }

    private void detach(ServerPlayer player, AttributeModifierInstance bonus, boolean notify) {
        AttributeInstance attribute = player.getAttribute(bonus.attribute());
        if (attribute == null) return;

        if (notify && bonus.attribute() == Attributes.MAX_HEALTH) {
            new SuppressHurtFlashPayload().sendTo(player);
        }
        attribute.removeModifier(bonus.modifier());
    }

    private int scale(int amount) {
        long total = (long) amount * Math.max(0, ServerConfiguration.getInstance().foodDrainRate) + drainRemainder;
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
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            return item != Items.AIR ? item : null;
        } catch (Exception exception) {
            return null;
        }
    }
}
