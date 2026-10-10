package com.than00ber.renourisheddelight.forge.event;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
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

    private static int leftHeight;

    @SubscribeEvent
    public static void onRenderGuiOverlayEventPre(RenderGuiOverlayEvent.Pre event) {
        ForgeGui gui = (ForgeGui) Minecraft.getInstance().gui;

        if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()) {
            leftHeight = gui.leftHeight;
        }
        if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
            event.setCanceled(true);
            Player player = Minecraft.getInstance().player;

            if (player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty() && !player.isPassenger()) {
                gui.rightHeight += ROW_HEIGHT;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayEventPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;
        if (!ClientConfiguration.getInstance().compactHealthBar) return;
        ForgeGui gui = (ForgeGui) Minecraft.getInstance().gui;

        if (gui.leftHeight != leftHeight) {
            gui.leftHeight = leftHeight + ROW_HEIGHT;
        }
    }
}
