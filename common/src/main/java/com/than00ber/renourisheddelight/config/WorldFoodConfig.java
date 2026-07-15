package com.than00ber.renourisheddelight.config;

import com.than00ber.renourisheddelight.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class WorldFoodConfig extends SavedData {

    private static final String ID = "renourisheddelight_food_config";
    
    public static WorldFoodConfig get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), ID);
    }

    private final List<FoodItemEntry> entries = new ArrayList<>();
    
    public List<FoodItemEntry> getEntries() {
        return entries;
    }

    public List<AttributeBonus> getAttributes(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        FoodItemEntry existing = entries.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);
        if (existing != null) return existing.attributes;

        List<AttributeBonus> attributes = CommonConfiguration.getInstance().getAttributes(item);
        entries.add(new FoodItemEntry(id, attributes));
        setDirty(true);
        return attributes;
    }

    private static SavedData.Factory<WorldFoodConfig> factory() {
        return new SavedData.Factory<>(WorldFoodConfig::createNew, WorldFoodConfig::load, DataFixTypes.LEVEL);
    }

    private static WorldFoodConfig createNew() {
        WorldFoodConfig data = new WorldFoodConfig();
        CommonConfiguration common = CommonConfiguration.getInstance();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item.components().get(DataComponents.FOOD) != null || common.hasConfiguredEntry(item)) {
                String id = BuiltInRegistries.ITEM.getKey(item).toString();
                data.entries.add(new FoodItemEntry(id, common.getAttributes(item)));
            }
        }
        data.setDirty(true);
        return data;
    }

    private static WorldFoodConfig load(CompoundTag tag, HolderLookup.Provider provider) {
        WorldFoodConfig data = new WorldFoodConfig();
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
            FoodItemEntry entry = new FoodItemEntry(entryTag.getString("Item"), bonuses, entryTag.getBoolean("Override"));
            data.entries.add(entry);
        }
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
        return tag;
    }
}
