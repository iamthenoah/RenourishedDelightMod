package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.config.ServerConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.StarvationEntry;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
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
import net.minecraft.world.entity.player.Player;
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
    private boolean replacing = true;
    private boolean categorized;

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

    public boolean isCategorized() {
        return categorized;
    }

    /**
     * Decides what eating the item would do. Relies only on synced state so the client reaches the
     * same outcome as the server before eating starts, and can preview which slot it would affect.
     */
    public EatingOutcome outcome(Item item, FoodCategories categories) {
        if (slots.stream().anyMatch(x -> x.item() == item)) return EatingOutcome.REPLENISH;

        if (categorized) {
            List<ConsumableFoodInstance> conflicts = conflicts(item, categories);
            if (conflicts.size() > 1) return EatingOutcome.CATEGORY_CONFLICT;
            if (conflicts.size() == 1) return replacing ? EatingOutcome.REPLACE_CATEGORY : EatingOutcome.CATEGORY_CONFLICT;
        }
        if (capacity <= 0 || slots.size() < capacity) return EatingOutcome.CONSUME;
        return replacing ? EatingOutcome.REPLACE_LOW : EatingOutcome.TOO_FULL;
    }

    // the slot the outcome tops up or swaps out, or the one in the way when eating is refused
    public @Nullable ConsumableFoodInstance target(EatingOutcome outcome, Item item, FoodCategories categories) {
        return switch (outcome) {
            case REPLENISH -> slots.stream().filter(x -> x.item() == item).findFirst().orElse(null);
            case REPLACE_CATEGORY, CATEGORY_CONFLICT -> conflicts(item, categories).stream().findFirst().orElse(null);
            case REPLACE_LOW -> slots.stream().min(Comparator.comparingInt(x -> x.duration() - x.time())).orElse(null);
            case CONSUME, TOO_FULL -> null;
        };
    }

    private List<ConsumableFoodInstance> conflicts(Item item, FoodCategories categories) {
        Set<ResourceLocation> eating = categories.get(item);
        if (eating.isEmpty()) return List.of();
        return slots.stream().filter(x -> categories.get(x.item()).stream().anyMatch(eating::contains)).toList();
    }

    public @Nullable Component reason(EatingOutcome outcome, Item item, FoodCategories categories) {
        if (outcome == EatingOutcome.TOO_FULL) {
            return Component.translatable("message.diet_full").withStyle(ChatFormatting.RED);
        }
        ConsumableFoodInstance blocking = outcome == EatingOutcome.CATEGORY_CONFLICT ? target(outcome, item, categories) : null;
        if (blocking == null) return null;
        Set<ResourceLocation> eating = categories.get(item);
        ResourceLocation category = categories.get(blocking.item()).stream().filter(eating::contains).findFirst().orElse(null);
        if (category == null) return null;
        return Component.translatable("message.category_conflict", FoodCategories.name(category), blocking.item().getDescription()).withStyle(ChatFormatting.RED);
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

    // checked on both sides before eating starts, the server also tells the player why
    public static boolean blocksEating(Player player, Item item) {
        if (player.getAbilities().invulnerable || !(player instanceof DietHolder holder)) return false;
        FoodCategories categories = FoodCategories.of(player.level());
        EatingOutcome outcome = holder.getDiet().outcome(item, categories);
        if (outcome.isAllowed()) return false;

        if (player instanceof ServerPlayer) {
            Component reason = holder.getDiet().reason(outcome, item, categories);
            if (reason != null) player.displayClientMessage(reason, true);
        }
        return true;
    }

    public void consume(ServerPlayer player, Item item) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        FoodCategories categories = FoodCategories.server();
        EatingOutcome outcome = outcome(item, categories);
        if (!outcome.isAllowed()) return;

        FoodConfig config = FoodConfigSavedData.get(server).getFoodConfig();
        int maxFoods = Math.max(1, ServerConfiguration.getInstance().maxConsumableFood);

        if (outcome == EatingOutcome.CONSUME) {
            addToSlot(player, eat(item, config));
        } else {
            replaceSlot(player, config, item, target(outcome, item, categories));
        }
        FoodProperties properties = item.components().get(DataComponents.FOOD);
        boolean harmful = properties != null && properties.effects().stream().anyMatch(x -> x.effect().getEffect().value().getCategory() == MobEffectCategory.HARMFUL);

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
        boolean replace = ServerConfiguration.getInstance().doReplaceLowestFood;
        boolean categories = ServerConfiguration.getInstance().doFoodCategories;
        boolean toggled = decaying != enabled || capacity != limit || replacing != replace || categorized != categories;
        decaying = enabled;
        capacity = limit;
        replacing = replace;
        categorized = categories;

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
        ConsumableFoodInstance instance = eat(item, config);
        slots.set(index, instance);
        attach(player, instance);
    }

    private void attach(ServerPlayer player, ConsumableFoodInstance instance) {
        for (AttributeModifierInstance bonus : instance.attributes()) {
            AttributeInstance attribute = player.getAttribute(bonus.attribute());

            if (attribute != null) {
                attribute.addPermanentModifier(bonus.modifier());

                if (bonus.attribute().value() == Attributes.MAX_HEALTH.value()) {
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

        if (notify && bonus.attribute().value() == Attributes.MAX_HEALTH.value()) {
            NetworkManager.sendToPlayer(player, new SuppressHurtFlashPayload());
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
        compoundTag.putBoolean("Replacing", diet.replacing);
        compoundTag.putBoolean("Categorized", diet.categorized);
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
        diet.replacing = !tag.contains("Replacing") || tag.getBoolean("Replacing");
        diet.categorized = tag.getBoolean("Categorized");
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
