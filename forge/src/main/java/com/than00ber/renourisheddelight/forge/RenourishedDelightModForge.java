package com.than00ber.renourisheddelight.forge;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.compat.client.ConfigMenuScreen;
import com.than00ber.renourisheddelight.food.Diet;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(RenourishedDelightMod.MOD_ID)
public final class RenourishedDelightModForge {

    public RenourishedDelightModForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(RenourishedDelightMod.MOD_ID, bus);
        RenourishedDelightMod.init();

        DeferredRegister<EntityDataSerializer<?>> serializers = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, RenourishedDelightMod.MOD_ID);
        serializers.register("diet", () -> Diet.DATA_SERIALIZER);
        serializers.register(bus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigMenuScreen(screen))));
    }
}
