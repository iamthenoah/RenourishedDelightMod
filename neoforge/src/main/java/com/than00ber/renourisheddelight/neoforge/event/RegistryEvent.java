package com.than00ber.renourisheddelight.neoforge.event;

import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class RegistryEvent {

    @SubscribeEvent
    public static void onRegisterBrewingRecipesEvent(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, Items.BEEF, PotionRegistry.NOURISHMENT);
    }
}