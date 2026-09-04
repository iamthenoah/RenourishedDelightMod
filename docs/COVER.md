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

## Game Rules

The mod adds seven game rules for server-wide customization:

| Game Rule                                       | Default | Description |
|-------------------------------------------------|---------| --- |
| `renourisheddelight:playerStartingHearts`       | 20      | Base max health before any food bonuses |
| `renourisheddelight:maxConsumableFood`          | 3       | Maximum number of foods active at once |
| `renourisheddelight:foodDrainRate`              | 100     | How fast active foods tick down, in percent (50 is half speed, 0 never drains) |
| `renourisheddelight:regenHealthTickInterval`    | 60      | Ticks between natural health regeneration (three times faster while nourished) |
| `renourisheddelight:regenDelayAfterDamage`      | 60      | Ticks to wait after taking damage before natural regen can resume |
| `renourisheddelight:nourishmentDurationPercent` | 10      | Nourishment duration as a % of the shortest active food, 0 disables Nourishment |
| `renourisheddelight:doNourishment`              | false   | Whether to give the player the Nourishment effect when full |
| `renourisheddelight:doStarvation`               | true    | Applies the configured starvation effects while a player has no active food |

Everything else that used to be its own game rule is now either derived from these or a fixed part of the system: the Hunger effect, health regen and sleeping all drain food at a fixed cost scaled by `foodDrainRate`, and natural regen is disabled while a player has no active food.

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

</details>

<details>
<summary>Common Config</summary>

| Option | Default | Description |
| --- | --- | --- |
| `config.foods` | `[]` | Per-item attribute bonuses. Only items you customize are listed; anything left out falls back to a max health bonus derived from its nutrition and saturation. An entry is the full definition for that item, so listing only a swim speed bonus grants only swim speed |
| `config.multipliers` | `[]` | Per-attribute duration multipliers applied when a bonus is granted. Attributes left out use `1.0`; stored per-item durations are unaffected |
| `config.starvation` | slowness, mining fatigue, weakness | Effects applied in stages while a player has no active food |

</details>

## Compatibility

Works out of the box with vanilla food and is built to play nicely with other food mods, including [Farmer's Delight](https://github.com/vectorwing/FarmersDelight). Presets for supported mods ship with the mod itself and can be tweaked per-item from the config screen.

## Feedback & Issues

Found a bug or have a suggestion? Open an issue on the [GitHub repo](https://github.com/iamthenoah/RenourishedDelightMod/issues).
