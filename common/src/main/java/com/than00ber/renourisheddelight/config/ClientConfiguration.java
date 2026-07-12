package com.than00ber.renourisheddelight.config;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = RenourishedDelightMod.MOD_ID + "/client")
public final class ClientConfiguration implements ConfigData {

    public static void init() {
        AutoConfig.register(ClientConfiguration.class, JanksonConfigSerializer::new);
    }

    public static ClientConfiguration getInstance() {
        return AutoConfig.getConfigHolder(ClientConfiguration.class).getConfig();
    }

    @ConfigEntry.Gui.Tooltip
    @Comment("Horizontal pixel offset for the food display UI (default: 0)")
    public int foodBarOffsetX = 0;

    @ConfigEntry.Gui.Tooltip
    @Comment("Vertical pixel offset for the food display UI (default: 0)")
    public int foodBarOffsetY = 0;

    @ConfigEntry.Gui.Tooltip
    @Comment("Item ID to sample for the golden-effect color palette (default: minecraft:golden_carrot)")
    public String goldenPaletteItem = "minecraft:golden_carrot";

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to render the active food items panel next to the inventory screen (default: true)")
    public boolean showFoodDisplayInInventory = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Cache generated item icon atlases to disk, so resource reloads skip re-rendering every icon when nothing changed (default: true)")
    public boolean enableAtlasCache = true;
}
