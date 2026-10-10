package com.than00ber.renourisheddelight.forge.client;

import com.than00ber.renourisheddelight.compat.client.ConfigMenuScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class ForgeClientSetup {

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigMenuScreen(screen)));
    }
}
