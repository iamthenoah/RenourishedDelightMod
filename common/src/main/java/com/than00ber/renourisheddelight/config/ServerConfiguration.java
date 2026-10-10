package com.than00ber.renourisheddelight.config;

import com.than00ber.renourisheddelight.RenourishedDelightMod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = RenourishedDelightMod.MOD_ID + "/server")
public final class ServerConfiguration implements ConfigData {
    public static void init() {
        AutoConfig.register(ServerConfiguration.class, JanksonConfigSerializer::new);
    }

    public static ServerConfiguration getInstance() {
        return AutoConfig.getConfigHolder(ServerConfiguration.class).getConfig();
    }

    @Comment("Whether eating the same food repeatedly shortens how long its bonuses last, recovering as you eat a variety of other foods. (default: false)")
    public boolean doNutritionDecay = false;

    @Comment("Whether skipping the night by sleeping drains food, scaled by how much of the night was skipped. Applies to every player, not only the ones who slept. (default: true)")
    public boolean doSleepFoodDrain = true;

    @Comment("Whether eating with every slot full and every active food still above 95% grants the Nourishment effect, lasting as long as the shortest active food and warding off harmful effects. (default: false)")
    public boolean doNourishment = false;

    @Comment("Applies the configured starvation effects when the player goes without food. (default: true)")
    public boolean doStarvation = true;

    @Comment("Prevents the player from sprinting if no food has been consumed. (default: false)")
    public boolean doPreventSprint = false;

    @Comment("Base max health a player has before any food bonuses are applied. (default: 20)")
    public int playerStartingHealth = 20;

    @Comment("Maximum number of foods a player can have active at the same time. (default: 3)")
    public int maxConsumableFood = 3;

    @Comment("How fast active foods tick down, as a percentage. 100 is normal, 50 is half speed, 0 stops food from draining. (default: 100)")
    public int foodDrainRate = 100;

    @Comment("Number of ticks between natural health regeneration. Nourishment makes this three times faster. (default: 60)")
    public int regenHealthTickInterval = 60;

    @Comment("Number of ticks to wait after taking damage before natural health regeneration can resume. (default: 60)")
    public int regenDelayAfterDamage = 60;

    @Comment("Percentage of its duration a food loses each time you eat it, and wins back once it leaves your recently eaten list. (default: 1)")
    public int nutritionDecayRate = 1;

    @Comment("How many different foods you must eat before an earlier one starts recovering from its nutrition decay. (default: 3)")
    public int nutritionDecayWindow = 3;

    @Comment("Lowest percentage of its configured duration a food can be worn down to by repeated eating. (default: 10)")
    public int nutritionDecayFloor = 10;
}
