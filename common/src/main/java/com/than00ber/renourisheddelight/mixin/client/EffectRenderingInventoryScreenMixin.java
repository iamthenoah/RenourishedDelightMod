package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.client.atlas.Texture;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlas;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    @Shadow @Final private static ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE;
    @Shadow @Final private static ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE;

    @Unique private static final int PANEL_LARGE_WIDTH = 120;
    @Unique private static final int PANEL_SMALL_WIDTH = 32;
    @Unique private static final int PANEL_HEIGHT = 32;
    @Unique private static final int PANEL_GAP = 2;
    @Unique private static final int ICON_SIZE = 18;
    @Unique private static final int ICON_MARGIN = 6;
    @Unique private static final int TEXT_GAP = 4;
    @Unique private static final int TEXT_MAX_WIDTH = 90;

    public EffectRenderingInventoryScreenMixin(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "renderEffects", at = @At("HEAD"))
    private void renourisheddelight$renderEffects(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo callback) {
        if (!ClientConfiguration.getInstance().showFoodDisplayInInventory) return;
        if (!(Minecraft.getInstance().player instanceof DietHolder holder)) return;

        List<ConsumableFoodInstance> slots = holder.getDiet().getSlots().stream()
                .sorted(Comparator.comparingInt((ConsumableFoodInstance x) -> x.duration() - x.time()).reversed())
                .toList();
        if (slots.isEmpty()) return;

        int available = leftPos - PANEL_GAP;
        if (available < PANEL_SMALL_WIDTH) return;
        boolean large = available >= PANEL_LARGE_WIDTH;
        int panelWidth = large ? PANEL_LARGE_WIDTH : PANEL_SMALL_WIDTH;
        int x = leftPos - PANEL_GAP - panelWidth;
        int iconX = large ? x + panelWidth - ICON_SIZE - ICON_MARGIN : x + 7;
        int textRight = iconX - TEXT_GAP;
        int rowHeight = slots.size() > 5 ? Math.max(1, 132 / Math.max(1, slots.size() - 1)) : PANEL_HEIGHT + 1;

        Player player = (Player) Minecraft.getInstance().player;
        Font font = Minecraft.getInstance().font;
        TextureAtlas atlas = TextureAtlasResourceLoader.getInstance().getLargeAtlas();
        boolean hunger = player.hasEffect(MobEffects.HUNGER);
        boolean nourished = player.hasEffect(EffectRegistry.nourishment());
        int y = topPos;
        ConsumableFoodInstance hovered = null;

        for (ConsumableFoodInstance slot : slots) {
            guiGraphics.blitSprite(large ? EFFECT_BACKGROUND_LARGE_SPRITE : EFFECT_BACKGROUND_SMALL_SPRITE, x, y, panelWidth, PANEL_HEIGHT);

            if (!large && mouseX >= x && mouseX < x + panelWidth && mouseY >= y && mouseY < y + PANEL_HEIGHT) {
                hovered = slot;
            }
            if (atlas != null) {
                Texture[] textures = atlas.getTextures(slot.item());

                if (textures != null && textures.length > 0) {
                    textures[nourished ? 4 : hunger ? 1 : 0].render(guiGraphics, iconX, y + 7, 0xFFFFFFFF);
                    if (hunger) textures[3].render(guiGraphics, iconX, y + 7, 0xFF12410B);
                }
            }
            if (large) {
                Component name = truncate(font, slot.item().getDescription());
                String time = StringUtil.formatTickDuration(slot.duration() - slot.time(), 20);

                guiGraphics.drawString(font, name, textRight - font.width(name), y + 6, 0xFFFFFF);
                guiGraphics.drawString(font, time, textRight - font.width(time), y + 16, 0xA0A0A0);
            }
            y += rowHeight;
        }

        if (hovered != null) {
            Component name = hovered.item().getDescription();
            Component time = Component.literal(StringUtil.formatTickDuration(hovered.duration() - hovered.time(), 20)).withStyle(ChatFormatting.GRAY);
            guiGraphics.renderComponentTooltip(font, List.of(name, time), mouseX, mouseY);
        }
    }

    @Unique
    private static Component truncate(Font font, Component name) {
        if (font.width(name) <= TEXT_MAX_WIDTH) return name;
        String shortened = font.plainSubstrByWidth(name.getString(), TEXT_MAX_WIDTH - font.width("…")).stripTrailing();
        return Component.literal(shortened + "…").setStyle(name.getStyle());
    }
}
