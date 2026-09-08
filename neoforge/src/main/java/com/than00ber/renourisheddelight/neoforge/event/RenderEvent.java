package com.than00ber.renourisheddelight.neoforge.event;

import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(Dist.CLIENT)
public final class RenderEvent {

    private static final int ROW_HEIGHT = 10;

    private static int leftHeight;

    @SubscribeEvent
    public static void onRenderGuiLayerEventPre(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            leftHeight = Minecraft.getInstance().gui.leftHeight;
        }
        if (event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) {
            event.setCanceled(true);
            Player player = Minecraft.getInstance().player;

            if (player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty() && !player.isPassenger()) {
                Minecraft.getInstance().gui.rightHeight += ROW_HEIGHT;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayerEventPost(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) return;
        if (!ClientConfiguration.getInstance().compactHealthBar) return;
        Gui gui = Minecraft.getInstance().gui;

        if (gui.leftHeight != leftHeight) {
            gui.leftHeight = leftHeight + ROW_HEIGHT;
        }
    }
}
