package com.than00ber.renourisheddelight.forge.event;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.client.overlay.HealthBarCompat;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RenourishedDelightMod.MOD_ID, value = Dist.CLIENT)
public final class RenderEvent {

    private static final int ROW_HEIGHT = 10;
    private static final int FOOD_RIGHT_OFFSET = 91;

    private static int leftHeight;

    @SubscribeEvent
    public static void onRenderGuiOverlayEventPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        ForgeGui gui = (ForgeGui) minecraft.gui;

        if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()) {
            leftHeight = gui.leftHeight;
        }
        if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
            // cancelling also skips the overlay's Post event, which Farmer's Delight draws its nourishment hunger icons on
            event.setCanceled(true);
            Player player = minecraft.player;

            // same conditions the vanilla food overlay renders under, so the bar takes the hunger bar's row
            // (honouring rightHeight changes from other mods) and stays visible in vehicles without hearts
            if (player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty()
                    && !(player.getVehicle() instanceof LivingEntity)
                    && !minecraft.options.hideGui
                    && gui.shouldDrawSurvivalElements()) {
                int width = event.getWindow().getGuiScaledWidth();
                int height = event.getWindow().getGuiScaledHeight();
                FoodBarOverlay.render(event.getGuiGraphics(), width / 2 + FOOD_RIGHT_OFFSET, height - gui.rightHeight);
                gui.rightHeight += ROW_HEIGHT;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayEventPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() && HealthBarCompat.compactHealthBar()) {
            ForgeGui gui = (ForgeGui) Minecraft.getInstance().gui;

            if (gui.leftHeight != leftHeight) {
                gui.leftHeight = leftHeight + ROW_HEIGHT;
            }
        }
    }
}
