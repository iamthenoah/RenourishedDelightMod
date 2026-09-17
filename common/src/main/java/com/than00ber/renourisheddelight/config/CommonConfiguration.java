package com.than00ber.renourisheddelight.config;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.data.FoodConfig;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = RenourishedDelightMod.MOD_ID + "/common")
public final class CommonConfiguration implements ConfigData, FoodConfigHolder {

    public static void init() {
        AutoConfig.register(CommonConfiguration.class, JanksonConfigSerializer::new);
    }

    public static CommonConfiguration getInstance() {
        return AutoConfig.getConfigHolder(CommonConfiguration.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(CommonConfiguration.class).save();
    }

    @ConfigEntry.Gui.Excluded
    @Comment("""
    Defaults used when a new world is created. Existing worlds keep their own copy; edit those from the in-game config screen.

    "foods" overrides what an item grants when eaten. Items left out fall back to a max health bonus derived from their nutrition and saturation. An entry is the full definition for that item, so listing only a swim speed bonus grants only swim speed. Durations are in ticks (20 = 1 second) and "operation" is one of add_value, add_multiplied_base or add_multiplied_total.
    [
      {
        item: "minecraft:golden_apple",
        attributes: [
          { attribute: "minecraft:generic.max_health", operation: "add_value", amount: 4.0, duration: 6000 }
        ]
      }
    ]

    "multipliers" scales the duration of every bonus for an attribute. Attributes left out use 1.0.
    [
      { attribute: "minecraft:generic.max_health", multiplier: 1.5 }
    ]

    "starvation" lists the effects applied while a player has no active food. Each stage adds its effect once the player has starved for "after" ticks, and earlier stages gain a level with every later stage reached, up to their own "max". Leave the list empty to disable starvation effects.
    [
      { effect: "minecraft:slowness", after: 3600, amplifier: 1, max: 3 }
    ]
    """)
    public FoodConfig config = new FoodConfig();

    @Override
    public FoodConfig getFoodConfig() {
        return config;
    }

    @Override
    public void setFoodConfig(FoodConfig updated) {
        config.copyFrom(updated);
        save();
    }
}
