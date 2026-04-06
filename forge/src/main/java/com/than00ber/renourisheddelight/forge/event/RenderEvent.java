package com.than00ber.renourisheddelight.forge.event;

import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class RenderEvent {

    @SubscribeEvent
    public static void onRenderGuiOverlayEventPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
            event.setCanceled(true);

            if (Minecraft.getInstance().player instanceof DietHolder holder && !holder.getDiet().getSlots().isEmpty()) {
                ((ForgeGui) Minecraft.getInstance().gui).rightHeight += 10;
            }
        }
    }
}