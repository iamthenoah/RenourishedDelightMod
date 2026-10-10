package com.than00ber.renourisheddelight.fabric.compat.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.than00ber.renourisheddelight.compat.client.ConfigMenuScreen;

public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigMenuScreen::new;
    }
}
