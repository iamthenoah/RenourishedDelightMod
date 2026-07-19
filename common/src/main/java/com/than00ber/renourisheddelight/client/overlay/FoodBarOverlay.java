package com.than00ber.renourisheddelight.client.overlay;

import com.mojang.blaze3d.platform.Window;
import com.than00ber.renourisheddelight.client.atlas.Texture;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlas;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import com.than00ber.renourisheddelight.compat.client.HudPositionScreen;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FoodBarOverlay implements ClientGuiEvent.RenderHud {

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
    public void renderHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
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
                boolean nourished = player.hasEffect(EffectRegistry.NOURISHMENT);
                renderSlots(graphics, atlas, new Point(x, y), slots, blink, hunger, nourished);
            }
        }
    }

    private boolean updateBlink(int count) {
        int tick = Minecraft.getInstance().gui.getGuiTicks();

        if (count < previousFoodCount) {
            foodBlinkEndTick = tick + 20;
        }
        previousFoodCount = count;
        return foodBlinkEndTick > tick && ((foodBlinkEndTick - tick) / 3) % 2 == 1;
    }

    public static void renderPreview(GuiGraphicsExtractor graphics, Point pos, List<ConsumableFoodInstance> slots) {
        TextureAtlas atlas = TextureAtlasResourceLoader.getInstance().getMiniAtlas();

        if (atlas != null && !slots.isEmpty()) {
            renderSlots(graphics, atlas, pos, slots, false, false, false);
        }
    }

    public static int countIconSlots(List<ConsumableFoodInstance> slots) {
        return computeShares(merge(slots)).values().stream().mapToInt(Integer::intValue).sum();
    }

    private static void renderSlots(GuiGraphicsExtractor graphics, TextureAtlas atlas, Point pos, List<ConsumableFoodInstance> slots, boolean blink, boolean hunger, boolean nourished) {
        int tick = Minecraft.getInstance().gui.getGuiTicks();
        int globalIndex = 0;

        for (Map.Entry<ConsumableFoodInstance, Integer> entry : computeShares(merge(slots)).entrySet()) {
            renderFood(graphics, atlas, pos, entry.getKey(), entry.getValue(), tick, blink, hunger, nourished, globalIndex);
            pos.x += 8 * entry.getValue();
            globalIndex += entry.getValue();
        }
    }

    private static void renderFood(GuiGraphicsExtractor graphics, TextureAtlas atlas, Point pos, ConsumableFoodInstance instance, int size, int tick, boolean blink, boolean hunger, boolean nourished, int globalIndexStart) {
        Texture[] textures = atlas.getTextures(instance.item());

        if (textures != null) {
            float fillRatio = 1.0f - ((float) instance.time() / (float) instance.duration());
            int width = Math.round(size * 8 * fillRatio);

            for (int i = 0; i < size; i++) {
                int offset = pos.y + computeWobbleOffset(instance, globalIndexStart + i, tick, hunger, nourished);
                textures[2].render(graphics, pos.x + i * 8, offset, 0xFF282828);
            }
            graphics.pose().pushPose();
            graphics.enableScissor(pos.x, pos.y, pos.x + width, pos.y + 9);

            for (int i = 0; i < size; i++) {
                int offset = pos.y + computeWobbleOffset(instance, globalIndexStart + i, tick, hunger, nourished);
                textures[nourished ? 4 : hunger ? 1 : 0].render(graphics, pos.x + i * 8, offset, 0xFFFFFFFF);
            }
            graphics.disableScissor();
            graphics.pose().popPose();

            for (int i = 0; i < size; i++) {
                int color = blink ? 0xFFFFFFFF : hunger ? 0xFF12410B : 0xFF000000;
                int offset = pos.y + computeWobbleOffset(instance, globalIndexStart + i, tick, hunger, nourished);
                textures[3].render(graphics, pos.x + i * 8, offset, color);
            }
        }
    }

    private static List<ConsumableFoodInstance> merge(List<ConsumableFoodInstance> slots) {
        Map<Item, ConsumableFoodInstance> merged = new LinkedHashMap<>();

        for (ConsumableFoodInstance instance : slots) {
            ConsumableFoodInstance existing = merged.get(instance.item());

            if (existing == null) {
                merged.put(instance.item(), instance.copy());
            } else {
                existing.attributes().addAll(instance.attributes());
            }
        }
        return new ArrayList<>(merged.values());
    }

    private static Map<ConsumableFoodInstance, Integer> computeShares(List<ConsumableFoodInstance> merged) {
        int totalDuration = merged.stream().mapToInt(ConsumableFoodInstance::duration).sum();
        Map<ConsumableFoodInstance, Integer> result = new LinkedHashMap<>();
        int remainingSlots = 10;

        if (merged.isEmpty()) {
            return result;
        }
        for (ConsumableFoodInstance instance : merged) {
            result.put(instance, 1);
            remainingSlots--;
        }
        if (remainingSlots <= 0) {
            return result;
        }
        for (ConsumableFoodInstance instance : merged) {
            if (remainingSlots == 0) break;

            float ratio = (float) instance.duration() / (float) totalDuration;
            int extra = Math.round(ratio * 10) - 1;

            if (extra > remainingSlots) {
                extra = remainingSlots;
            }
            if (extra < 0) extra = 0;

            result.put(instance, result.get(instance) + extra);
            remainingSlots -= extra;
        }
        if (remainingSlots > 0) {
            ConsumableFoodInstance last = merged.getLast();
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
}
