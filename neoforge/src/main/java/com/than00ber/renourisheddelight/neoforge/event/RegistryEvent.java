package com.than00ber.renourisheddelight.neoforge.event;

import com.than00ber.renourisheddelight.config.datapack.FoodPresetsPackSource;
import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class RegistryEvent {

    @SubscribeEvent
    public static void onRegisterBrewingRecipesEvent(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, Items.BEEF, PotionRegistry.NOURISHMENT);
    }

    @SubscribeEvent
    public static void onAddPackFindersEvent(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(new FoodPresetsPackSource());
        }
    }
}