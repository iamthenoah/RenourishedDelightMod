package com.than00ber.renourisheddelight.neoforge.event;

import com.than00ber.renourisheddelight.data.FoodConfigDataLoader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber
public final class ReloadListenerEvent {

    @SubscribeEvent
    public static void onAddReloadListenerEvent(AddReloadListenerEvent event) {
        event.addListener(new FoodConfigDataLoader());
    }
}
