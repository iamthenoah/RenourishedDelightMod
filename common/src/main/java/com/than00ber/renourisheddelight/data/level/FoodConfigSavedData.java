package com.than00ber.renourisheddelight.data.level;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.DurationMultiplierEntry;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.FoodConfigReloadListener;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class FoodConfigSavedData extends SavedData implements FoodConfigHolder {

    private static final String ID = "renourisheddelight_food_config";

    private final List<FoodItemEntry> entries;
    private final List<DurationMultiplierEntry> multipliers;

    private FoodConfigSavedData(List<FoodItemEntry> entries, List<DurationMultiplierEntry> multipliers) {
        this.entries = entries;
        this.multipliers = multipliers;
    }

    public static FoodConfigSavedData get(MinecraftServer server) {
        SavedData.Factory<FoodConfigSavedData> factory = new SavedData.Factory<>(
                FoodConfigSavedData::create,
                (tag, provider) -> load(tag),
                DataFixTypes.LEVEL);
        return server.overworld().getDataStorage().computeIfAbsent(factory, ID);
    }

    @Override
    public List<FoodItemEntry> getFoodConfig() {
        return entries;
    }

    @Override
    public List<DurationMultiplierEntry> getMultiplierConfig() {
        return multipliers;
    }

    @Override
    public void update(List<FoodItemEntry> updatedEntries, List<DurationMultiplierEntry> updatedMultipliers) {
        entries.clear();
        multipliers.clear();
        updatedEntries.forEach(x -> entries.add(x.copy()));
        updatedMultipliers.forEach(x -> multipliers.add(x.copy()));
        setDirty();
    }

    public boolean applyPresets(List<FoodItemEntry> presets) {
        boolean changed = false;

        for (FoodItemEntry preset : presets) {
            FoodItemEntry entry = FoodItemEntry.get(entries, preset.item);

            if (entry == null) {
                entries.add(preset.copy());
                changed = true;
            } else if (preset.override) {
                entry.attributes = preset.copy().attributes;
                entry.override = true;
                changed = true;
            } else {
                for (AttributeBonus bonus : preset.attributes) {
                    if (entry.attributes.stream().noneMatch(x -> bonus.attribute.equals(x.attribute))) {
                        entry.attributes.add(bonus.copy());
                        changed = true;
                    }
                }
            }
        }
        if (changed) setDirty();
        return changed;
    }

    private static FoodConfigSavedData create() {
        CommonConfiguration common = CommonConfiguration.getInstance();
        List<FoodItemEntry> entries = new ArrayList<>();
        List<DurationMultiplierEntry> multipliers = new ArrayList<>();
        common.foodItemConfigurations.forEach(x -> entries.add(x.copy()));
        common.durationMultipliers.forEach(x -> multipliers.add(x.copy()));
        return withPresets(entries, multipliers);
    }

    private static FoodConfigSavedData load(CompoundTag tag) {
        List<FoodItemEntry> entries = new ArrayList<>();
        List<DurationMultiplierEntry> multipliers = new ArrayList<>();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            List<AttributeBonus> bonuses = new ArrayList<>();
            ListTag bonusList = entryTag.getList("Attributes", Tag.TAG_COMPOUND);

            for (int j = 0; j < bonusList.size(); j++) {
                CompoundTag bonusTag = bonusList.getCompound(j);
                bonuses.add(new AttributeBonus(
                        bonusTag.getString("Attribute"),
                        bonusTag.getString("Operation"),
                        bonusTag.getDouble("Amount"),
                        bonusTag.getInt("Duration")));
            }
            entries.add(new FoodItemEntry(entryTag.getString("Item"), bonuses, entryTag.getBoolean("Override")));
        }
        ListTag multiplierList = tag.getList("Multipliers", Tag.TAG_COMPOUND);

        for (int i = 0; i < multiplierList.size(); i++) {
            CompoundTag multiplierTag = multiplierList.getCompound(i);
            multipliers.add(new DurationMultiplierEntry(
                    multiplierTag.getString("Attribute"),
                    multiplierTag.getDouble("Multiplier")));
        }
        return withPresets(entries, multipliers);
    }

    private static FoodConfigSavedData withPresets(List<FoodItemEntry> entries, List<DurationMultiplierEntry> multipliers) {
        FoodConfigSavedData data = new FoodConfigSavedData(entries, multipliers);
        data.applyPresets(FoodConfigReloadListener.PRESETS);
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        for (FoodItemEntry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("Item", entry.item);
            entryTag.putBoolean("Override", entry.override);
            ListTag bonusList = new ListTag();

            for (AttributeBonus bonus : entry.attributes) {
                CompoundTag bonusTag = new CompoundTag();
                bonusTag.putString("Attribute", bonus.attribute);
                bonusTag.putString("Operation", bonus.operation);
                bonusTag.putDouble("Amount", bonus.amount);
                bonusTag.putInt("Duration", bonus.duration);
                bonusList.add(bonusTag);
            }
            entryTag.put("Attributes", bonusList);
            list.add(entryTag);
        }
        tag.put("Entries", list);
        ListTag multiplierList = new ListTag();

        for (DurationMultiplierEntry multiplier : multipliers) {
            CompoundTag multiplierTag = new CompoundTag();
            multiplierTag.putString("Attribute", multiplier.attribute);
            multiplierTag.putDouble("Multiplier", multiplier.multiplier);
            multiplierList.add(multiplierTag);
        }
        tag.put("Multipliers", multiplierList);
        return tag;
    }
}
