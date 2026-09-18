<div align="center">

<img width="474px" alt="renourisheddelight" src="https://cdn.modrinth.com/data/cached_images/83a0cc25224318f32d26b2e09ef2588a3f4be507.png" />

Renourished Delight changes how eating works. Instead of one hunger bar that every food fills the same way, you can have several different foods active at once, each giving its own buffs for a limited time. Eat a variety of food and keep those buffs stacked up, instead of just shoving down whatever's in your hotbar to top off hunger.

<video src="https://i.imgur.com/NmSzAuo.mp4" autoplay muted loop playsinline></video>

</div>

<br/>

## How it works

Normally in Minecraft, food just refills your hunger bar and saturation. With this mod, every food item you eat gets its own slot (up to a limit you can configure) and while it's active, it gives you bonuses like extra max health, speed or other attribute boosts. Each slot counts down over time and drops off once its duration runs out, so you'll want to keep eating to keep your buffs going instead of eating once and forgetting about it.

Different foods give different bonuses and last different amounts of time, so eating a variety of food actually matters now instead of just grabbing whatever gives the most hunger points. _Food attributes works for every mod out of the box_, but you can always change that on the config page.

## Nourishment

Once every food slot is full, eating again grants **Nourishment**. It speeds up natural health regen and stops the extra food drain that normally comes with the Hunger effect. Eating a food you already have active tops it back up, and eating a new food while full replaces whichever slot has the least time left, so you are never blocked from eating.

## Nutrition Decay

Eating the same thing over and over stops working as well. Every time a food takes a slot it decays by **1%**, and that comes straight off its duration and off every attribute bonus it grants. Keep eating it and it keeps dropping. Nothing resets it on its own.

What wins it back is variety. The mod remembers the last **3** different foods you ate, and anything that falls off that list recovers 1% every time you eat something else. So a food you have not touched in a while slowly climbs back to full strength while you are eating other things.

The practical effect is that three food sources is not enough. Rotating through exactly your slot count keeps the same foods on the recently eaten list permanently, so they never recover — you need a wider pantry than you have slots. A food never drops below **10%** of its configured values, so a favourite you lean on too hard gets weak but never useless.

Set `doNutritionDecay` to false to turn the whole system off; the rate, the window and the floor are all game rules too. Topping up a food you already have active costs nothing, though it does respect what the food is currently worth — a worn-down food tops back up to its reduced duration, not its full one. Each player has their own values and their own recently eaten list, and both are saved with the world.

Item tooltips show what you would actually get rather than the configured numbers, with the shortfall on the last line, so you can watch a food weaken as you lean on it.

## Game Rules

The mod adds fifteen game rules for server-wide customization:

| Game Rule | Default | Description |
| --- | --- | --- |
| `renourisheddelight:playerStartingHearts` | 20 | Base max health before any food bonuses |
| `renourisheddelight:maxConsumableFood` | 3 | Maximum number of foods active at once |
| `renourisheddelight:foodDrainRate` | 100 | How fast active foods tick down, in percent (50 is half speed, 0 never drains) |
| `renourisheddelight:regenHealthTickInterval` | 60 | Ticks between natural health regeneration (three times faster while nourished) |
| `renourisheddelight:regenDelayAfterDamage` | 60 | Ticks to wait after taking damage before natural regen can resume |
| `renourisheddelight:nourishmentDurationPercent` | 10 | Nourishment duration as a % of the shortest active food |
| `renourisheddelight:nutritionDecayRate` | 1 | Percent of its duration and attribute strength a food decays by each time it takes a slot, and recovers once it leaves the recently eaten list |
| `renourisheddelight:nutritionDecayWindow` | 3 | How many different foods you must eat before an earlier one starts recovering from its nutrition decay |
| `renourisheddelight:nutritionDecayFloor` | 10 | Lowest percent of its configured duration and attributes a food can be worn down to |
| `renourisheddelight:doNutritionDecay` | true | Whether eating the same food repeatedly decays its duration and attribute bonuses |
| `renourisheddelight:doSleepFoodDrain` | true | Whether skipping the night drains food, scaled by how much of the night was skipped, for every player |
| `renourisheddelight:doNourishment` | false | Whether eating while full grants the Nourishment effect |
| `renourisheddelight:doStarvation` | true | Applies the configured starvation effects while a player has no active food |
| `renourisheddelight:doReplenish` | true | Whether eating a food you already have active tops it back up, once it is at 50% or less remaining |
| `renourisheddelight:doReplaceLowest` | true | Whether eating a new food while every slot is full replaces the food with the least time left |

Eating resolves in one of three ways: a food you already have active is topped back up (`doReplenish`, once it is at 50% or less remaining), a food you do not have active takes a free slot, and if there is no free slot it replaces the one with the least time left (`doReplaceLowest`). When a rule turns its case off the food cannot be eaten at all, and the player is told why. Food that carries status effects is still edible in that situation, granting its effects but no slot.

The Hunger effect, health regen and sleeping all drain food at a fixed cost scaled by `foodDrainRate`, and natural regen is disabled while a player has no active food.

## Configuration

All settings are also editable in-game: open the pause menu (or ModMenu on Fabric / the mod list on NeoForge) and pick **Renourished Delight** to get a screen.

![config-item-listing](https://cdn.modrinth.com/data/cached_images/067191504a5fac74c9be1aba64a52c8fdbdcd0b9.png)

![config-item-bonuses](https://cdn.modrinth.com/data/cached_images/c6407d2edbe51f8d90fec84ab2ae2cb6ab0fd8d2.png)

The configs are also available as config files:

<details>
<summary>Client Config</summary>

| Option | Default | Description |
| --- | --- | --- |
| `foodBarOffsetX` | 0 | Horizontal pixel offset for the food display UI |
| `foodBarOffsetY` | 0 | Vertical pixel offset for the food display UI |
| `goldenPaletteItem` | `minecraft:golden_carrot` | Item ID used to sample the color palette for the golden-effect tint |
| `showFoodDisplayInInventory` | false | Whether to render the active food items panel next to the inventory screen |
| `clipOddMaxHealthHeart` | true | Whether to clip the last heart's unfillable half when max health is odd, instead of showing a half heart that can never fill |
| `compactHealthBar` | false | Whether to draw health as a single row of 10 hearts that fill proportionally to max health, instead of stacking extra heart rows |

</details>

<details>
<summary>Common Config</summary>

| Option | Default | Description |
| --- | --- | --- |
| `config.foods` | `[]` | Per-item attribute bonuses. Only items you customize are listed; anything left out falls back to a max health bonus derived from its nutrition and saturation. An entry is the full definition for that item, so listing only a swim speed bonus grants only swim speed |
| `config.multipliers` | `[]` | Per-attribute duration multipliers applied when a bonus is granted. Attributes left out use `1.0`; stored per-item durations are unaffected |
| `config.starvation` | slowness, mining fatigue, weakness | Effects applied in stages while a player has no active food |

</details>

## Commands

`/renourisheddelight clear [<targets>]` empties a player's food bar, dropping every active food and its bonuses. With no target it clears your own. It needs permission level 2, so operators and single-player worlds with cheats on.

## Compatibility

Works out of the box with vanilla food and is built to play nicely with other food mods, including [Farmer's Delight](https://github.com/vectorwing/FarmersDelight). Presets for supported mods ship with the mod itself and can be tweaked per-item from the config screen.

## Feedback & Issues

Found a bug or have a suggestion? Open an issue on the [GitHub repo](https://github.com/iamthenoah/RenourishedDelightMod/issues).
