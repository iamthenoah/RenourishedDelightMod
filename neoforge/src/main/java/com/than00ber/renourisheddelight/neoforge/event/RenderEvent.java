package com.than00ber.renourisheddelight.neoforge.event;

import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber
public final class RenderEvent {

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) {
            event.setCanceled(true);

            if (Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty()) {
                Minecraft.getInstance().gui.rightHeight += 10;
            }
        }
    }
}