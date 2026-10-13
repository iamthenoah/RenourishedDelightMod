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
For those looking for a more in-depth eating experience, you can enable Nutrition Decay (via the <code>doNutritionDecay</code> server config). This will add an extra dimension to the game by nudging you to find a range of food source to cycle on. The more varied your diet, the less impact it will have over time.

This can be fun if you have multiple food mods and want to find a reason to cook various meals.</p>

</td>
</tr>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="18">
<tr>
<td width="100%" valign="middle">
<h3>Diet Restrictions (Optional)</h3>
<p>
Enable Diet Restrictions (via the <code>doDietRestrictions</code> server config) to stop players from filling every slot with the same kind of meal. Each food is grouped by its main ingredients, such as beef, fish or berries, and you can't have two active foods that share one. Eating a food that shares an ingredient with exactly one active food swaps that food out instead.

There is nothing to set up. Ingredients are worked out from the item tags food mods already ship with, and from recipes, so a steak and a beef stew both count as beef. Hold <b>Shift</b> over a food to see its ingredients and whether you can eat it right now.</p>

</td>
</tr>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="18">
<tr>
<td width="55%" valign="middle">
<h3>Highly Configurable</h3>
<p>
Not happy with the attributes or duration? Want to make certain food items special? The in-game config menu lets you edit the bonuses of every food item, scale bonus durations per attribute, set up starvation effects, and tweak both the server rules and your own HUD. This mod is shipped with presets for certain mods like <a href="https://github.com/vectorwing/FarmersDelight">Farmer's Delight</a> and <a href="https://github.com/team-abnormals/neapolitan">Neapolitan</a> to work with <a href="https://modrinth.com/mod/irons-spells-n-spellbooks">Iron's Spells 'n Spellbooks</a> and the RPG series. If you want presets for a mod, make an issue <a href="https://github.com/iamthenoah/RenourishedDelightMod/issues">here</a> and I'll gladly look into it :)
</p>
</td>
<td width="45%" valign="middle">
<img width="100%" alt="food item settings" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/highly_configurable.png" />
</td>
</tr>
</table>

<br/>

<details>
<summary><b>Configs</b></summary>
<br/>
<p>Everything below can be edited from the in-game config menu, or directly in the files under <code>config/renourisheddelight/</code>.</p>

<p><b>Client</b> (<code>client.json5</code>) — your own display, never synced to servers.</p>
<p><code>foodBarOffsetX</code> / <code>foodBarOffsetY</code> — moves the food bar, in pixels. Easiest to set by dragging it in the config menu. Default 0.</p>
<p><code>goldenPaletteItem</code> — item whose colors tint the food icons gold while Nourishment is active. Default <code>minecraft:golden_carrot</code>.</p>
<p><code>showFoodDisplayInInventory</code> — shows your active foods next to the inventory. Default false.</p>
<p><code>clipOddMaxHealthHeart</code> — hides the half heart that can never fill when max health is odd. Default true.</p>
<p><code>compactHealthBar</code> — keeps health to a single row of 10 hearts that fill proportionally. Default false.</p>

<p><b>Server</b> (<code>server.json5</code>) — gameplay rules. On a dedicated server, edit the file on the server.</p>
<p><code>playerStartingHealth</code> — base max health before any food bonuses. Default 20.</p>
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
<p><code>doPreventSprint</code> — players can't sprint until they've eaten something. Default false.</p>
<p><code>doReplaceLowestFood</code> — eating with every slot full replaces the food closest to running out. When off, you have to wait for a slot to free up. Default true.</p>
<p><code>doDietRestrictions</code> — eating a food that shares a main ingredient (beef, fish, berries...) with an active food swaps that food out, as long as only one active food shares it and <code>doReplaceLowestFood</code> is on. Otherwise you can't eat it. Ingredients are worked out automatically from item tags and recipes, so steak and beef stew both count as beef. Default false.</p>

<p><b>Common</b> (<code>common.json5</code>) — the food bonuses, duration multipliers and starvation effects new worlds start with. Each world keeps its own copy once created, which operators can edit in-game from the food, duration and starvation screens.</p>
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
    ]
  }
]</code></pre>

<p><code>attributes</code> is optional. <code>operation</code> is <code>add_value</code>, <code>add_multiplied_base</code> or <code>add_multiplied_total</code>, where 0.15 means 15%. <code>duration</code> is in ticks, 20 per second. Placeable foods use the item id, like <code>minecraft:cake</code>. The max health bonus derived from the food's nutrition and saturation is kept unless you declare your own <code>minecraft:generic.max_health</code> entry.</p>

<p><b>Diet restrictions</b></p>

<p>Restrictions come from the <code>c:foods/*</code> item tags, so a datapack can reshape them with ordinary tag files. Add items to an existing tag, or make a new tag to create a new restriction.</p>

<pre><code>your_pack/
  pack.mcmeta
  data/c/tags/item/foods/beef.json</code></pre>

<pre><code>{
  "replace": false,
  "values": [ "minecraft:beef" ]
}</code></pre>

<p>Tag the raw ingredient rather than every dish: recipes carry it into anything made from it, so this one entry covers steak, stews and sandwiches. When a food is in both a broad tag and a narrower one, like <code>c:foods/raw_meat</code> and <code>c:foods/beef</code>, only the narrower one counts. <code>c:foods/edible_when_placed</code>, <code>c:foods/food_poisoning</code> and <code>c:foods/golden</code> are ignored since they describe how a food behaves, not what is in it. The name shown in tooltips comes from the <code>tag.item.c.foods.beef</code> translation key, falling back to the tag's name. Run <code>/reload</code> after editing.</p>

<p><b>Generating a preset with AI</b></p>

<p>Paste this into an assistant along with the mod's item list, then check the numbers before shipping.</p>

<pre><code>Write a Renourished Delight food preset for Minecraft 1.21.1.

Output one JSON array, no commentary. Each entry:
{ "item": "namespace:id",
  "attributes": [ { "attribute": "...", "operation": "add_value|add_multiplied_base|add_multiplied_total",
                    "amount": 0, "duration": 0 } ] }

- Cover every edible item, including placeable ones like cakes, pies and stews.
  Use the item id, not the block id.
- Balance against crafting cost: rarer or multi step foods earn more.
- Keep it restrained. +1 to +3 max health is normal, +4 is a showpiece. Percentages
  sit between 0.05 and 0.2. Durations 2400 to 6000 ticks (20 ticks = 1 second).
- Theme each food after its ingredients instead of giving everything max health.
  Meat to damage and health, vegetables and soups to speed or mining speed,
  sweets to short bursts, fish to underwater traits.
- Use an occasional negative attribute to pay for an unusually strong food.
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
