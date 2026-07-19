package com.than00ber.renourisheddelight.config.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

public final class WorldFoodConfig extends SavedData {

    private static final Codec<AttributeBonus> BONUS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("Attribute").forGetter(x -> x.attribute),
            Codec.STRING.fieldOf("Operation").forGetter(x -> x.operation),
            Codec.DOUBLE.fieldOf("Amount").forGetter(x -> x.amount),
            Codec.INT.fieldOf("Duration").forGetter(x -> x.duration)
    ).apply(instance, AttributeBonus::new));

    private static final Codec<FoodItemEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("Item").forGetter(x -> x.item),
            BONUS_CODEC.listOf().fieldOf("Attributes").forGetter(x -> x.attributes),
            Codec.BOOL.fieldOf("Override").forGetter(x -> x.override)
    ).apply(instance, FoodItemEntry::new));

    private static final MapCodec<WorldFoodConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ENTRY_CODEC.listOf().fieldOf("Entries").forGetter(WorldFoodConfig::getEntries)
    ).apply(instance, WorldFoodConfig::new));

    public static final SavedDataType<WorldFoodConfig> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("renourisheddelight", "food_config"),
            WorldFoodConfig::createNew,
            CODEC,
            DataFixTypes.LEVEL
    );

    public static WorldFoodConfig get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    private final List<FoodItemEntry> entries = new ArrayList<>();

    private WorldFoodConfig() {
    }

    private WorldFoodConfig(List<FoodItemEntry> entries) {
        this.entries.addAll(entries);
    }

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
}
