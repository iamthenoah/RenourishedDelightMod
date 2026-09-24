<img width="100%" alt="Renourished Delight" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/title.png" />

<p align="center">Replaces the hunger bar with a beautiful food bar that shows what you ate</p>

<p align="center">
<img width="50%" alt="food bar" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/plug-and-play.png" />
</p>

<p align="center">
The hunger bar becomes a food bar. Every food item consumed takes its own slot and gives you bonuses. Compatible accross all the food mods you can think of.
</p>

<br/>

<table width="100%" border="0" cellspacing="0" cellpadding="18">
<tr>
<td width="45%" valign="middle">
<img width="100%" alt="automatic downscale" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/automatic_downscale.png" />
</td>
<td width="55%" valign="middle">
<h3>Cross-mod/resourcepack Compatible</h3>
<p>
Food icons are generated, each one is rendered from the item's own texture at load and downscaled to fit the bar, so modded food, custom resource packs and items added after the fact all show up correctly with no extra art needed.</p>
</td>
</tr>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="18">
<tr>
<td width="55%" valign="middle">
<h3>Food Bonuses</h3>
<p>
Every food item consumed gives you increased max health for a set duration. Like the textures, these attributes are also generated based on the food's nutrition and saturation. The more varity you eat, the more max health you'll receive.</p>
</td>
<td width="45%" valign="middle">
<img width="100%" alt="food tooltip" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/tooltip.png" />
</td>
</tr>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="18">
<tr>
<td width="45%" valign="middle">
<img width="100%" alt="nutrition decay" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/nutrition.png" />
</td>
<td width="55%" valign="middle">
<h3>Nutrition Decay (Optional)</h3>
<p>
For those looking for a more indepth eating experience, you can enable Nutrition Decay (via the gamerule `doNutritionDecay`). This will add an extra dimension to the game by nudging you to find a range of food source to cycle on. The more varied your diet, the less impact it will have over time.

This can be fun if you have multiple food mods and want to find a reason to cook various meals.</p>

</td>
</tr>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="18">
<tr>
<td width="55%" valign="middle">
<h3>Highly Configurable</h3>
<p>
Not happy with the attributes or duration? Want to make certain food items special? There are some config screens available to change everything, including amplifiers and some client configs. This mod is shipped with presets for certain mods like <a href="https://github.com/vectorwing/FarmersDelight">Farmer's Delight</a> and <a href="https://github.com/team-abnormals/neapolitan">Neapolitan</a> to work with <a href="https://modrinth.com/mod/irons-spells-n-spellbooks">Iron's Spells 'n Spellbooks</a> and the RPG series. If you want presets for a mod, make an issue <a href="https://github.com/iamthenoah/RenourishedDelightMod/issues">here</a> and I'll gladly look into it :)
</p>
</td>
<td width="45%" valign="middle">
<img width="100%" alt="food item settings" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/highly_configurable.png" />
</td>
</tr>
</table>

<br/>

<details>
<summary><b>Game rules</b></summary>
<br/>
<p><code>playerStartingHearts</code> — base max health before any food bonuses. Default 20.</p>
<p><code>maxConsumableFood</code> — how many foods can be active at once. Default 3.</p>
<p><code>foodDrainRate</code> — how fast slots tick down, in percent. 50 is half speed, 0 never drains. Default 100.</p>
<p><code>regenHealthTickInterval</code> — ticks between natural regen. Default 60.</p>
<p><code>regenDelayAfterDamage</code> — ticks to wait after damage before regen resumes, skipped while nourished. Default 60.</p>
<p><code>nutritionDecayRate</code> — percent of duration lost per meal, and won back once the food leaves your recent list. Default 1.</p>
<p><code>nutritionDecayWindow</code> — how many other foods you must eat before one starts recovering. Default 3.</p>
<p><code>nutritionDecayFloor</code> — lowest percent of its duration a food can drop to. Default 10.</p>
<p><code>doNutritionDecay</code> — repeating a food shortens how long its bonuses last. Default false.</p>
<p><code>doSleepFoodDrain</code> — skipping the night drains everyone's food. Default true.</p>
<p><code>doNourishment</code> — a full bar of fresh food grants Nourishment. Default false.</p>
<p><code>doStarvation</code> — apply starvation effects when the bar is empty. Default true.</p>
</details>

<details>
<summary><b>Commands</b></summary>
<br/>
<p>Both need permission level 2.</p>
<p><code>/renourisheddelight clear [&lt;targets&gt;]</code> empties a player's food bar.</p>
<p><code>/renourisheddelight decay reset [&lt;targets&gt;] [&lt;item&gt;]</code> puts decayed foods back to full duration.</p>
</details>

<details>
<summary><b>Modpacks</b></summary>
<br/>

<p>Presets are JSON in a datapack. Whatever you define becomes that item's default in the config screen, and players can still edit it or reset back to it.</p>

<pre><code>your_pack/
  pack.mcmeta
  data/renourisheddelight/foodpresets/anything.json</code></pre>

<p><code>pack.mcmeta</code> needs <code>"pack_format": 48</code> for 1.21.1. File names do not matter and you can split entries across as many as you like.</p>

<pre><code>[
  {
    "item": "minecraft:cake",
    "attributes": [
      { "attribute": "minecraft:generic.max_health", "operation": "add_value", "amount": 4.0, "duration": 6000 }
    ],
    "effects": [
      { "effect": "minecraft:night_vision", "level": 1, "duration": 3600 }
    ]
  }
]</code></pre>

<p><code>attributes</code> and <code>effects</code> are both optional. <code>operation</code> is <code>add_value</code>, <code>add_multiplied_base</code> or <code>add_multiplied_total</code>, where 0.15 means 15%. <code>duration</code> is in ticks, 20 per second. Placeable foods use the item id, like <code>minecraft:cake</code>. Anything you leave out falls back to the value derived from that food's nutrition and saturation.</p>

<p><b>Generating a preset with AI</b></p>

<p>Paste this into an assistant along with the mod's item list, then check the numbers before shipping.</p>

<pre><code>Write a Renourished Delight food preset for Minecraft 1.21.1.

Output one JSON array, no commentary. Each entry:
{ "item": "namespace:id",
  "attributes": [ { "attribute": "...", "operation": "add_value|add_multiplied_base|add_multiplied_total",
                    "amount": 0, "duration": 0 } ],
  "effects":    [ { "effect": "...", "level": 1, "duration": 0 } ] }
Both lists are optional.

- Cover every edible item, including placeable ones like cakes, pies and stews.
  Use the item id, not the block id.
- Balance against crafting cost: rarer or multi step foods earn more.
- Keep it restrained. +1 to +3 max health is normal, +4 is a showpiece. Percentages
  sit between 0.05 and 0.2. Durations 2400 to 6000 ticks (20 ticks = 1 second).
- Theme each food after its ingredients instead of giving everything max health.
  Meat to damage and health, vegetables and soups to speed or mining speed,
  sweets to short bursts, fish to underwater traits.
- Use an occasional negative attribute to pay for an unusually strong food.
- Effects sparingly, and shorter than the attribute durations.
- Only real ids: minecraft:generic.max_health, minecraft:generic.movement_speed,
  minecraft:generic.armor, minecraft:generic.attack_damage,
  minecraft:generic.knockback_resistance, minecraft:generic.safe_fall_distance,
  minecraft:player.block_break_speed.

Item list:
</code></pre>

</details>

<p align="center">
Bugs and ideas go on the <a href="https://github.com/iamthenoah/RenourishedDelightMod/issues">issue tracker</a>.
</p>
