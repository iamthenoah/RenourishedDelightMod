<table align="center" width="1200" border="0" cellspacing="0" cellpadding="0">
<tr>
<td>

<img width="100%" alt="Renourished Delight" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/title.png" />

<h3 align="center">One hunger bar for everything you eat? Not anymore.</h3>

<p align="center">
<img width="80%" alt="food bar" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/plug-and-play.png" />
</p>

<p align="center">
The hunger bar becomes a food bar. Every meal takes its own slot and keeps feeding you bonuses until it runs out, so what you eat matters as much as how much. Drop the jar in and it works, no datapacks and no setup.
</p>

<br/>

<table width="100%" border="0" cellspacing="0" cellpadding="18">
<tr>
<td colspan="2" valign="middle" align="center">
<img width="70%" alt="automatic downscale" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/automatic_downscale.png" />
<h3>Works with every mod and every resource pack</h3>
<p>Food icons are not hand drawn. Each one is rendered from the item's own texture at load and downscaled to fit the bar, so modded food, custom resource packs and items added after the fact all show up correctly with no extra art.</p>
</td>
</tr>
<tr>
<td width="50%" valign="top">
<img width="100%" alt="food tooltip" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/tooltip.png" />
<h3>Every food is worth something different</h3>
<p>Bonuses are derived from a food's nutrition and saturation, so a sandwich and a raw potato are not the same meal. The tooltip spells out exactly what you get and for how long before you take a bite.</p>
</td>
<td width="50%" valign="top">
<img width="100%" alt="nutrition decay" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/nutrition.png" />
<h3>Optional nutrition decay</h3>
<p>Turn it on and eating the same thing over and over wears it down. Its nutrition tier drops and the bonus runs shorter, down to a tenth, until you rotate it out and let it recover. Off by default.</p>
</td>
</tr>
<tr>
<td colspan="2" valign="middle" align="center">
<img width="70%" alt="food item settings" src="https://raw.githubusercontent.com/iamthenoah/RenourishedDelightMod/master/docs/assets/highly_configurable.png" />
<h3>Configure everything</h3>
<p>Set what any item grants, attributes or effects, straight from the mod list. Edit one world or the global defaults used for new ones, ship your own presets in a datapack, and tune the rest with twelve game rules. Presets for <a href="https://github.com/vectorwing/FarmersDelight">Farmer's Delight</a> and <a href="https://github.com/team-abnormals/neapolitan">Neapolitan</a> are built in.</p>
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

<p align="center">
Bugs and ideas go on the <a href="https://github.com/iamthenoah/RenourishedDelightMod/issues">issue tracker</a>.
</p>

</td>
</tr>
</table>
