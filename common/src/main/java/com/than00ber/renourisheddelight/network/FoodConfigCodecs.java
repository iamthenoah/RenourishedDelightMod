package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

final class FoodConfigCodecs {

    static final StreamCodec<RegistryFriendlyByteBuf, AttributeBonus> BONUS_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, bonus -> bonus.attribute,
            ByteBufCodecs.STRING_UTF8, bonus -> bonus.operation,
            ByteBufCodecs.DOUBLE, bonus -> bonus.amount,
            ByteBufCodecs.VAR_INT, bonus -> bonus.duration,
            AttributeBonus::new);

    static final StreamCodec<RegistryFriendlyByteBuf, FoodItemEntry> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, entry -> entry.item,
            BONUS_CODEC.apply(ByteBufCodecs.list()), entry -> entry.attributes,
            ByteBufCodecs.BOOL, entry -> entry.override,
            FoodItemEntry::new);

    static final StreamCodec<RegistryFriendlyByteBuf, List<FoodItemEntry>> ENTRY_LIST_CODEC = ENTRY_CODEC.apply(ByteBufCodecs.list());
}
