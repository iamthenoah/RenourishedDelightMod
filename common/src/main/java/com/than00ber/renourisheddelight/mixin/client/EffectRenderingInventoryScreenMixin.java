package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.client.atlas.Texture;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlas;
import com.than00ber.renourisheddelight.client.atlas.TextureAtlasResourceLoader;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

    @Shadow @Final private static Identifier EFFECT_BACKGROUND_LARGE_SPRITE;
    @Shadow @Final private static Identifier EFFECT_BACKGROUND_SMALL_SPRITE;

    @Unique private int savedTopPos = -1;

    public EffectRenderingInventoryScreenMixin(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "renderEffects", at = @At("HEAD"))
    private void renderEffects(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo callback) {
        savedTopPos = -1;

        if (ClientConfiguration.getInstance().showFoodDisplayInInventory) {
            Player player = Minecraft.getInstance().player;

            if (player instanceof DietHolder holder) {
                List<ConsumableFoodInstance> slots = holder.getDiet().getSlots().stream()
                        .sorted(Comparator.comparingInt((ConsumableFoodInstance x) -> x.duration() - x.time()).reversed())
                        .toList();
                if (slots.isEmpty()) return;

                int x = leftPos + imageWidth + 2;
                int availableWidth = width - x;
                if (availableWidth < 32) return;
                boolean large = availableWidth >= 120;
                int rowHeight = slots.size() > 5 ? Math.max(1, 132 / Math.max(1, slots.size() - 1)) : 33;

                Font font = Minecraft.getInstance().font;
                TextureAtlas atlas = TextureAtlasResourceLoader.getInstance().getLargeAtlas();
                int k = topPos;

                for (ConsumableFoodInstance slot : slots) {
                    if (large) {
                        guiGraphics.blitSprite(EFFECT_BACKGROUND_LARGE_SPRITE, x, k, 120, 32);
                    } else {
                        guiGraphics.blitSprite(EFFECT_BACKGROUND_SMALL_SPRITE, x, k, 32, 32);
                    }
                    if (atlas != null) {
                        Texture[] textures = atlas.getTextures(slot.item());

                        if (textures != null && textures.length > 0) {
                            boolean hunger = player.hasEffect(MobEffects.HUNGER);
                            boolean nourished = player.hasEffect(EffectRegistry.NOURISHMENT);
                            int index = nourished ? 4 : hunger ? 1 : 0;
                            textures[index].render(guiGraphics, x + (large ? 6 : 7), k + 7, 0xFFFFFFFF);

                            if (hunger) {
                                textures[3].render(guiGraphics, x + (large ? 6 : 7), k + 7, 0xFF12410B);
                            }
                        }
                    }
                    if (large) {
                        Component name = slot.item().getDescription();

                        if (font.width(name) > 90) {
                            String truncated = font.plainSubstrByWidth(name.getString(), 90 - font.width("…")).stripTrailing();
                            name = Component.literal(truncated + "…").setStyle(name.getStyle());
                        }
                        int remainingTicks = slot.duration() - slot.time();
                        String time = StringUtil.formatTickDuration(remainingTicks, 20);
                        guiGraphics.drawString(font, name, x + 10 + 18, k + 6, 0xFFFFFF);
                        guiGraphics.drawString(font, time, x + 10 + 18, k + 6 + 10, 0xA0A0A0);
                    }
                    k += rowHeight;
                }
                savedTopPos = topPos;
                topPos += rowHeight * slots.size();
            }
        }
    }

    @Inject(method = "renderEffects", at = @At("RETURN"))
    private void renderEffectsReturn(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo callback) {
        if (savedTopPos != -1) {
            topPos = savedTopPos;
            savedTopPos = -1;
        }
    }
}