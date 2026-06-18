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
        }

        // Push other right-side elements up to make room if diet is active
        if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR) || event.getName().equals(VanillaGuiLayers.JUMP_METER)) {
            if (Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty()) {
                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(0, -10, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPost(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR) || event.getName().equals(VanillaGuiLayers.JUMP_METER)) {
            if (Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty()) {
                event.getGuiGraphics().pose().popPose();
            }
        }
    }
}