package com.than00ber.renourisheddelight.data.level;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FoodConfigSavedData extends SavedData {

    private static final String ID = "renourisheddelight_food_config";

    private final ServerLevel level;

    private FoodConfigSavedData(ServerLevel level) {
        this.level = level;
    }

    public static FoodConfigHolder get(MinecraftServer server) {
        ServerLevel level = server.overworld();
        storage(level);
        return (FoodConfigHolder) level;
    }

    public static void markDirty(MinecraftServer server) {
        storage(server.overworld()).setDirty();
    }
    
    private static FoodConfigSavedData storage(ServerLevel level) {
        SavedData.Factory<FoodConfigSavedData> factory = new SavedData.Factory<>(
                () -> populateDefaults(level),
                (tag, provider) -> load(tag, level),
                DataFixTypes.LEVEL
        );
        return level.getDataStorage().computeIfAbsent(factory, ID);
    }

    public static boolean refreshFromPresets(MinecraftServer server) {
        FoodConfigHolder holder = get(server);
        Map<String, FoodItemEntry> byId = new HashMap<>();
        for (FoodItemEntry entry : holder.getFoodConfig()) {
            byId.put(entry.item, entry);
        }
        CommonConfiguration common = CommonConfiguration.getInstance();
        boolean changed = false;

        for (Item item : BuiltInRegistries.ITEM) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            FoodItemEntry existing = byId.get(id);

            if (existing == null) {
                if (item.components().get(DataComponents.FOOD) != null || common.hasFoodItemEntry(item)) {
                    holder.getFoodConfig().add(new FoodItemEntry(id, AttributeBonus.computeDefaultBonuses(item)));
                    changed = true;
                }
            } else if (!existing.override) {
                existing.attributes = AttributeBonus.computeDefaultBonuses(item);
                changed = true;
            }
        }
        if (changed) {
            markDirty(server);
        }
        return changed;
    }

    private static FoodConfigSavedData populateDefaults(ServerLevel level) {
        FoodConfigSavedData storage = new FoodConfigSavedData(level);
        FoodConfigHolder holder = (FoodConfigHolder) level;
        CommonConfiguration common = CommonConfiguration.getInstance();
        List<FoodItemEntry> entries = holder.getFoodConfig();

        for (Item item : BuiltInRegistries.ITEM) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();

            if (entries.stream().noneMatch(x -> id.equals(x.item))) {
                if (item.components().get(DataComponents.FOOD) != null || common.hasFoodItemEntry(item)) {
                    entries.add(new FoodItemEntry(id, common.getFoodItemEntry(item).attributes));
                }
            }
        }
        storage.setDirty();
        return storage;
    }

    private static FoodConfigSavedData load(CompoundTag tag, ServerLevel level) {
        FoodConfigSavedData storage = new FoodConfigSavedData(level);
        List<FoodItemEntry> entries = new ArrayList<>();
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
        ((FoodConfigHolder) level).setFoodConfig(entries);
        return storage;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        for (FoodItemEntry entry : ((FoodConfigHolder) level).getFoodConfig()) {
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
        return tag;
    }
}
