package com.example.fabric;

import com.example.MyExampleMod;
import net.fabricmc.api.ModInitializer;

public final class MyExampleModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MyExampleMod.init();
    }
}
