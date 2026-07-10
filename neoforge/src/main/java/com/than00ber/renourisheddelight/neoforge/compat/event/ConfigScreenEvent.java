package com.than00ber.renourisheddelight.neoforge.compat.event;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.compat.client.ConfigMenuScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = RenourishedDelightMod.MOD_ID, dist = Dist.CLIENT)
public final class ConfigScreenEvent {

    public ConfigScreenEvent(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (m, s) -> new ConfigMenuScreen(s));
    }
}
