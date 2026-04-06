package com.example.forge;

import com.example.MyExampleMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MyExampleMod.MOD_ID)
public final class MyExampleModForge {
    
    public MyExampleModForge() {
        EventBuses.registerModEventBus(MyExampleMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        MyExampleMod.init();
    }
}
