package com.than00ber.renourisheddelight.client.overlay;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.than00ber.renourisheddelight.client.atlas.Texture;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlas;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import com.than00ber.renourisheddelight.compat.client.HudPositionScreen;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FoodBarOverlay implements ClientGuiEvent.RenderHud {

    private static final int MAX_ICONS = 10;
    private static final int ICON_WIDTH = 8;
    private static final int ICON_HEIGHT = 9;
    private static final int BLINK_DURATION = 20;
    private static final int PULSE_DURATION = 20;
    private static final float PULSE_MIN_ALPHA = 0.25F;

    private int previousFoodCount;
    private int foodBlinkEndTick;

    public static void init() {
        ClientGuiEvent.RENDER_HUD.register(new FoodBarOverlay());
    }

    private boolean isVisible() {
        Minecraft minecraft = Minecraft.getInstance();

        return minecraft.player != null && !(minecraft.player.getVehicle() != null
                && minecraft.player.getVehicle().showVehicleHealth())
                && minecraft.gameMode != null
                && !minecraft.options.hideGui
                && minecraft.gameMode.canHurtPlayer()
                && minecraft.getCameraEntity() instanceof Player
                && !(minecraft.screen instanceof HudPositionScreen);
    }

    @Override
    public void renderHud(GuiGraphics graphics, DeltaTracker deltaTracker) {
        TextureAtlas atlas = TextureAtlasResourceLoader.getInstance().getMiniAtlas();
        Player player = Minecraft.getInstance().player;

        if (isVisible() && atlas != null && player instanceof DietHolder holder) {
            List<ConsumableFoodInstance> slots = holder.getDiet().getSlots();

            if (!slots.isEmpty()) {
                Window window = Minecraft.getInstance().getWindow();
                int x = window.getGuiScaledWidth() / 2 + 10 + ClientConfiguration.getInstance().foodBarOffsetX;
                int y = window.getGuiScaledHeight() - 39 + ClientConfiguration.getInstance().foodBarOffsetY;
                boolean blink = updateBlink(slots.size());
                boolean hunger = player.hasEffect(MobEffects.HUNGER);
                boolean nourished = player.hasEffect(EffectRegistry.nourishment());
                renderSlots(graphics, atlas, x, y, slots, blink, hunger, nourished, preview(player, holder.getDiet(), deltaTracker));
            }
        }
    }

    private boolean updateBlink(int count) {
        int tick = Minecraft.getInstance().gui.getGuiTicks();

        if (count < previousFoodCount) {
            foodBlinkEndTick = tick + BLINK_DURATION;
        }
        previousFoodCount = count;
        return foodBlinkEndTick > tick && ((foodBlinkEndTick - tick) / 3) % 2 == 1;
    }

    public static void renderPreview(GuiGraphics graphics, int x, int y, List<ConsumableFoodInstance> slots) {
        TextureAtlas atlas = TextureAtlasResourceLoader.getInstance().getMiniAtlas();

        if (atlas != null && !slots.isEmpty()) {
            renderSlots(graphics, atlas, x, y, slots, false, false, false, Preview.NONE);
        }
    }

    private static Preview preview(Player player, Diet diet, DeltaTracker deltaTracker) {
        List<ConsumableFoodInstance> slots = diet.getSlots();
        Item item = targetItem(player);
        if (item == null) return Preview.NONE;

        int tick = Minecraft.getInstance().gui.getGuiTicks() % PULSE_DURATION;
        float time = (tick + deltaTracker.getGameTimeDeltaPartialTick(false)) / PULSE_DURATION;
        float wave = (1.0F - Mth.cos(time * Mth.TWO_PI)) / 2.0F;
        float alpha = Mth.lerp(wave, PULSE_MIN_ALPHA, 1.0F);

        if (isActive(slots, item)) return new Preview(item, null, alpha);
        if (diet.getCapacity() <= 0 || slots.size() < diet.getCapacity()) return Preview.NONE;

        return slots.stream()
                .min(Comparator.comparingInt(x -> x.duration() - x.time()))
                .map(x -> new Preview(null, x.item(), alpha))
                .orElse(Preview.NONE);
    }

    private static @Nullable Item targetItem(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.has(DataComponents.FOOD)) return stack.getItem();
        }
        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult result) || result.getType() != HitResult.Type.BLOCK) return null;

        Item item = player.level().getBlockState(result.getBlockPos()).getBlock().asItem();
        return isEatable(item) ? item : null;
    }

    private static boolean isEatable(Item item) {
        if (item.components().has(DataComponents.FOOD)) return true;
        return Minecraft.getInstance().getConnection() instanceof FoodConfigHolder holder && holder.getFoodConfig().entry(item) != null;
    }

    private static boolean isActive(List<ConsumableFoodInstance> slots, Item item) {
        return slots.stream().anyMatch(x -> x.item() == item);
    }

    public static int countIconSlots(List<ConsumableFoodInstance> slots) {
        return computeShares(slots).values().stream().mapToInt(Integer::intValue).sum();
    }

    private static void renderSlots(GuiGraphics graphics, TextureAtlas atlas, int x, int y, List<ConsumableFoodInstance> slots, boolean blink, boolean hunger, boolean nourished, Preview preview) {
        int tick = Minecraft.getInstance().gui.getGuiTicks();
        int globalIndex = 0;
        int cursor = x;

        RenderSystem.enableBlend();

        for (Map.Entry<ConsumableFoodInstance, Integer> entry : computeShares(slots).entrySet()) {
            renderFood(graphics, atlas, cursor, y, entry.getKey(), entry.getValue(), tick, blink, hunger, nourished, preview, globalIndex);
            cursor += ICON_WIDTH * entry.getValue();
            globalIndex += entry.getValue();
        }
        RenderSystem.disableBlend();
    }

    private static void renderFood(GuiGraphics graphics, TextureAtlas atlas, int x, int y, ConsumableFoodInstance instance, int size, int tick, boolean blink, boolean hunger, boolean nourished, Preview preview, int globalIndexStart) {
        Texture[] textures = atlas.getTextures(instance.item());

        if (textures != null) {
            float opacity = preview.fades(instance) ? preview.alpha() : 1.0F;
            float fillRatio = 1.0f - ((float) instance.time() / (float) instance.duration());
            int width = Math.round(size * ICON_WIDTH * fillRatio);

            for (int i = 0; i < size; i++) {
                int offset = y + computeWobbleOffset(instance, globalIndexStart + i, tick, hunger, nourished);
                textures[2].render(graphics, x + i * ICON_WIDTH, offset, fade(0xFF282828, opacity));
            }
            graphics.pose().pushPose();
            graphics.enableScissor(x, y, x + width, y + ICON_HEIGHT);

            for (int i = 0; i < size; i++) {
                int offset = y + computeWobbleOffset(instance, globalIndexStart + i, tick, hunger, nourished);
                textures[nourished ? 4 : hunger ? 1 : 0].render(graphics, x + i * ICON_WIDTH, offset, fade(0xFFFFFFFF, opacity));
            }
            graphics.disableScissor();
            graphics.pose().popPose();

            if (preview.tops(instance)) {
                for (int i = 0; i < size; i++) {
                    int offset = y + computeWobbleOffset(instance, globalIndexStart + i, tick, hunger, nourished);
                    textures[nourished ? 4 : hunger ? 1 : 0].render(graphics, x + i * ICON_WIDTH, offset, preview.color());
                }
            }
            for (int i = 0; i < size; i++) {
                int color = blink ? 0xFFFFFFFF : hunger ? 0xFF12410B : 0xFF000000;
                int offset = y + computeWobbleOffset(instance, globalIndexStart + i, tick, hunger, nourished);
                textures[3].render(graphics, x + i * ICON_WIDTH, offset, fade(color, opacity));
            }
        }
    }

    private static int fade(int color, float opacity) {
        return (Mth.floor(((color >> 24) & 0xFF) * opacity) << 24) | (color & 0xFFFFFF);
    }

    private static Map<ConsumableFoodInstance, Integer> computeShares(List<ConsumableFoodInstance> slots) {
        int totalDuration = slots.stream().mapToInt(ConsumableFoodInstance::duration).sum();
        Map<ConsumableFoodInstance, Integer> result = new LinkedHashMap<>();
        int remainingSlots = MAX_ICONS;

        if (slots.isEmpty()) {
            return result;
        }
        for (ConsumableFoodInstance instance : slots) {
            result.put(instance, 1);
            remainingSlots--;
        }
        if (remainingSlots <= 0) {
            return result;
        }
        for (ConsumableFoodInstance instance : slots) {
            if (remainingSlots == 0) break;

            float ratio = (float) instance.duration() / (float) totalDuration;
            int extra = Math.round(ratio * MAX_ICONS) - 1;

            if (extra > remainingSlots) {
                extra = remainingSlots;
            }
            if (extra < 0) extra = 0;

            result.put(instance, result.get(instance) + extra);
            remainingSlots -= extra;
        }
        if (remainingSlots > 0) {
            ConsumableFoodInstance last = slots.getLast();
            result.put(last, result.get(last) + remainingSlots);
        }
        return result;
    }

    private static int computeWobbleOffset(ConsumableFoodInstance food, int index, int tick, boolean hunger, boolean nourished) {
        if (nourished) return computeNourishmentWobble(index, tick);
        int timeLeft = food.duration() - food.time();
        float threeMinutes = 60 * 20 * 3;
        if (timeLeft > threeMinutes && !hunger) return 0;
        float lowFactor = hunger ? 1.0F : Mth.clamp(timeLeft / threeMinutes, 0.0F, 1.0F);
        int wobble = Mth.clamp(Math.round(lowFactor * 20), 1, 20);
        return tick % (wobble * 3 + 1) == 0 ? ((tick + index) % 2 == 0) ? 1 : -1 : 0;
    }

    private static int computeNourishmentWobble(int index, int tick) {
        int positionInCycle = tick % (25 + 20);
        return positionInCycle >= 25 ? 0 : index == positionInCycle ? -2 : 0;
    }

    private record Preview(@Nullable Item topped, @Nullable Item replaced, float alpha) {

        private static final Preview NONE = new Preview(null, null, 0.0F);

        private boolean tops(ConsumableFoodInstance instance) {
            return topped != null && instance.item() == topped;
        }

        private boolean fades(ConsumableFoodInstance instance) {
            return replaced != null && instance.item() == replaced;
        }

        private int color() {
            return (Mth.floor(alpha * 255.0F) << 24) | 0xFFFFFF;
        }
    }
}
