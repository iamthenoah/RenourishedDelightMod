package com.than00ber.renourisheddelight.forge;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RenourishedDelightMod.MOD_ID)
public final class RenourishedDelightModForge {
    
    public RenourishedDelightModForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(RenourishedDelightMod.MOD_ID, bus);
        RenourishedDelightMod.init();
    }
}
