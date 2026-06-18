package com.than00ber.renourisheddelight.food;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

public class ConsumableFoodInstance {

    public final Item item;
    public AttributeModifier hearts;
    public int duration;
    public int time;

    public ConsumableFoodInstance(Item item, AttributeModifier hearts, int duration, int time) {
        this.item = item;
        this.hearts = hearts;
        this.duration = duration;
        this.time = time;
    }

    public ConsumableFoodInstance copy() {
        return new ConsumableFoodInstance(item, hearts, duration, time);
    }

    public static CompoundTag save(ConsumableFoodInstance instance) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Item", BuiltInRegistries.ITEM.getKey(instance.item).toString());
        compoundTag.put("Hearts", instance.hearts.save());
        compoundTag.putInt("Duration", instance.duration);
        compoundTag.putInt("Time", instance.time);
        return compoundTag;
    }

    public static ConsumableFoodInstance load(CompoundTag compoundTag) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(compoundTag.getString("Item")));
        AttributeModifier hearts = AttributeModifier.load(compoundTag.getCompound("Hearts"));
        int duration = compoundTag.getInt("Duration");
        int time = compoundTag.getInt("Time");
        return new ConsumableFoodInstance(item, hearts, duration, time);
    }
}
