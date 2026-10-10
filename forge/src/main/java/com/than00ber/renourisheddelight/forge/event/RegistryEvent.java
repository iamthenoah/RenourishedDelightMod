package com.than00ber.renourisheddelight.forge.event;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.data.datapack.FoodPresetsPackSource;
import com.than00ber.renourisheddelight.registry.PotionRegistry;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = RenourishedDelightMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RegistryEvent {

    @SubscribeEvent
    public static void onFMLCommonSetupEvent(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(new NourishmentBrewingRecipe()));
    }

    @SubscribeEvent
    public static void onAddPackFindersEvent(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(new FoodPresetsPackSource());
        }
    }

    // mirrors vanilla's awkward + beef mix for every potion container, which forge 1.20.1 does not expose
    private static final class NourishmentBrewingRecipe implements IBrewingRecipe {

        @Override
        public boolean isInput(ItemStack input) {
            boolean container = input.is(Items.POTION) || input.is(Items.SPLASH_POTION) || input.is(Items.LINGERING_POTION);
            return container && PotionUtils.getPotion(input) == Potions.AWKWARD;
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            return ingredient.is(Items.BEEF);
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
            return PotionUtils.setPotion(new ItemStack(input.getItem()), PotionRegistry.nourishment());
        }
    }
}
