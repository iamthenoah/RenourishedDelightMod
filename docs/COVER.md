<div align="center">

<img width="474px" alt="renourisheddelight" src="https://cdn.modrinth.com/data/cached_images/83a0cc25224318f32d26b2e09ef2588a3f4be507.png" />

Renourished Delight changes how eating works. Instead of one hunger bar that every food fills the same way, you can have several different foods active at once, each giving its own buffs for a limited time. Eat a variety of food and keep those buffs stacked up, instead of just shoving down whatever's in your hotbar to top off hunger.

<video src="https://i.imgur.com/NmSzAuo.mp4" autoplay muted loop playsinline></video>

</div>

<br/>

## How it works

Normally in Minecraft, food just refills your hunger bar and saturation, full stop. With this mod, every food item you eat gets its own slot (up to a limit you can configure) and while it's active, it gives you bonuses like extra max health, speed, or other attribute boosts. Each slot counts down over time and drops off once its duration runs out, so you'll want to keep eating to keep your buffs going instead of eating once and forgetting about it.

Different foods give different bonuses and last different amounts of time, so eating a variety of food actually matters now instead of just grabbing whatever gives the most hunger points.

## Nourishment

If you eat food while you're already at your max number of active food slots, you get **Nourishment**. It speeds up your natural health regen a lot and stops the extra food drain that normally comes with the Hunger effect. It's basically a reward for keeping your food topped up instead of letting your buffs expire before eating again.

## Game Rules

The mod adds several game rules for server-wide customization:

| Game Rule | Default | Description |
| --- | --- | --- |
| `renourisheddelight:playerStartingHearts` | 20 | Hearts players start with when joining/respawning |
| `renourisheddelight:maxConsumableFood` | 3 | Maximum number of different foods active at once |
| `renourisheddelight:allowEatingTheSameItem` | false | Whether players can eat multiple of the same food |
| `renourisheddelight:replaceLowestFoodItem` | false | Whether eating food replaces the food with the least time remaining when full |
| `renourisheddelight:foodReplenishableThreshold` | 50 | % of a food's duration that must have passed before it can be eaten again to top it back up |
| `renourisheddelight:foodItemStacks` | true | Whether same food items tick down simultaneously |
| `renourisheddelight:hungerFoodDrain` | 2 | Extra food drained per second while the player has the Hunger effect |
| `renourisheddelight:regenHealthTickInterval` | 60 | Ticks between natural health regeneration |
| `renourisheddelight:regenHealthFoodDrain` | 3 | Food drained each time health regenerates |
| `renourisheddelight:applyNourishmentWhenFull` | true | Whether players receive the Nourishment effect when full |
| `renourisheddelight:nourishmentDurationPercent` | 10 | Nourishment's duration, as a % of the shortest remaining active food duration |
| `renourisheddelight:nourishmentRegenTickInterval` | 20 | Ticks between natural health regeneration while nourished (lower is faster) |
| `renourisheddelight:regenDelayAfterDamage` | 60 | Ticks to wait after taking damage before natural regen can resume |
| `renourisheddelight:sleepFoodDrain` | 12000 | Ticks of food drained for sleeping through a full night, scaled down for a partial night's sleep |
| `renourisheddelight:attackFoodDrain` | 0 | Extra ticks of food drained each time a player attacks |
| `renourisheddelight:jumpFoodDrain` | 0 | Extra ticks of food drained each time a player jumps |
| `renourisheddelight:sprintFoodDrain` | 0 | Extra ticks of food drained per second while a player is sprinting |
| `renourisheddelight:disableHealthRegenWhenHungry` | true | When the player has an empty stomach, health regen will be disabled. |

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
| `enableAtlasCache` | true | Cache generated item icon atlases to disk so resource reloads skip re-rendering every icon when nothing changed |

</details>

<details>
<summary>Common Config</summary>

| Option | Default | Description |
| --- | --- | --- |
| `foodItemConfigurations` | `[]` | Per-item attribute bonuses (auto-populated on first launch, editable from the in-game config screen) |

</details>

## Compatibility

Works out of the box with vanilla food and is built to play nicely with other food mods, including [Farmer's Delight](https://github.com/vectorwing/FarmersDelight). Presets for supported mods ship with the mod itself and can be tweaked per-item from the config screen.

## Feedback & Issues

Found a bug or have a suggestion? Open an issue on the [GitHub repo](https://github.com/iamthenoah/RenourishedDelightMod/issues).
