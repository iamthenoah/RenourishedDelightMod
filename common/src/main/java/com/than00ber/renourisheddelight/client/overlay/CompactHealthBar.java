package com.than00ber.renourisheddelight.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class CompactHealthBar {

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
    private static final int HEARTS = 10;
    private static final int SPACING = 8;
    private static final int SIZE = 9;
    private static final int WIDTH = HEARTS * SPACING;
    private static final int FULL = WIDTH + SIZE - SPACING;
    private static final int BOB_CYCLE = HEARTS + 15;
    private static final float JITTER_THRESHOLD = 0.2F;
    private static final int HARDCORE_ROW = SIZE * 5;

    // column indices of each heart type in the icons texture
    private static final int CONTAINER = 0;
    private static final int NORMAL = 2;
    private static final int POISONED = 4;
    private static final int WITHERED = 6;
    private static final int ABSORBING = 8;
    private static final int FROZEN = 9;

    public static void render(GuiGraphics graphics, Player player, RandomSource random, int left, int top, int tickCount, boolean regenerating, float maxHealth, int health, int displayHealth, int absorption, boolean highlight) {
        float max = Math.max(maxHealth, 1.0F);
        int row = player.level().getLevelData().isHardcore() ? HARDCORE_ROW : 0;
        int type = type(player);
        int shield = type == WITHERED ? WITHERED : ABSORBING;
        int bob = regenerating ? tickCount % BOB_CYCLE : -1;
        boolean jitter = health + absorption <= max * JITTER_THRESHOLD;
        int[] offsets = new int[HEARTS];

        for (int i = 0; i < HEARTS; i++) {
            offsets[i] = (jitter ? random.nextInt(2) : 0) - (i == bob ? 2 : 0);
        }
        int filled = fill(health, max);
        int shielded = fill(health + absorption, max);

        RenderSystem.enableBlend();
        blit(graphics, container(highlight), row, left, top, offsets, 0, FULL);
        blit(graphics, heart(shield, false), row, left, top, offsets, filled, shielded);
        if (highlight) blit(graphics, heart(type, true), row, left, top, offsets, 0, fill(displayHealth, max));
        blit(graphics, heart(type, false), row, left, top, offsets, 0, filled);
        RenderSystem.disableBlend();
    }

    private static void blit(GuiGraphics graphics, int u, int v, int left, int top, int[] offsets, int from, int to) {
        if (to <= from) return;

        for (int i = 0; i < HEARTS; i++) {
            int base = i * SPACING;
            int start = from >= base + SPACING ? SIZE : Math.max(from - base, 0);
            int end = to >= base + SPACING ? SIZE : Math.min(to - base, SIZE);
            if (end <= start) continue;
            int x = left + base;
            int y = top + offsets[i];

            graphics.enableScissor(x + start, y, x + end, y + SIZE);
            graphics.blit(ICONS, x, y, u, v, SIZE, SIZE);
            graphics.disableScissor();
        }
    }

    private static int fill(float value, float max) {
        if (value <= 0.0F) return 0;
        if (value >= max) return FULL;
        return Mth.ceil(value / max * WIDTH);
    }

    private static int type(Player player) {
        if (player.hasEffect(MobEffects.POISON)) return POISONED;
        if (player.hasEffect(MobEffects.WITHER)) return WITHERED;
        if (player.isFullyFrozen()) return FROZEN;
        return NORMAL;
    }

    private static int container(boolean blinking) {
        return u(CONTAINER, blinking ? 1 : 0);
    }

    private static int heart(int type, boolean blinking) {
        boolean canBlink = type == NORMAL || type == POISONED || type == WITHERED;
        return u(type, blinking && canBlink ? 2 : 0);
    }

    private static int u(int index, int variant) {
        return 16 + (index * 2 + variant) * SIZE;
    }
}
