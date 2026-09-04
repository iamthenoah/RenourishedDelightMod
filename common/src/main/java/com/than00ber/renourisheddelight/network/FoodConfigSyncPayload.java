package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.compat.client.AbstractFoodConfigScreen;
import com.than00ber.renourisheddelight.config.data.*;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record FoodConfigSyncPayload(FoodConfig config) implements CustomPacketPayload {

    private static final Type<FoodConfigSyncPayload> TYPE = new Type<>(RenourishedDelightMod.key("food_config_sync"));

    private static final StreamCodec<RegistryFriendlyByteBuf, AttributeBonus> BONUS_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, x -> x.attribute,
            ByteBufCodecs.STRING_UTF8, x -> x.operation,
            ByteBufCodecs.DOUBLE, x -> x.amount,
            ByteBufCodecs.VAR_INT, x -> x.duration,
            AttributeBonus::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, FoodItemEntry> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, x -> x.item,
            BONUS_CODEC.apply(ByteBufCodecs.list()), x -> x.attributes,
            FoodItemEntry::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, DurationMultiplierEntry> MULTIPLIER_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, x -> x.attribute,
            ByteBufCodecs.DOUBLE, x -> x.multiplier,
            DurationMultiplierEntry::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, StarvationEntry> STARVATION_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, x -> x.effect,
            ByteBufCodecs.VAR_INT, x -> x.after,
            ByteBufCodecs.VAR_INT, x -> x.amplifier,
            ByteBufCodecs.VAR_INT, x -> x.max,
            StarvationEntry::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, FoodConfig> CONFIG_CODEC = StreamCodec.composite(
            ENTRY_CODEC.apply(ByteBufCodecs.list()), x -> x.foods,
            MULTIPLIER_CODEC.apply(ByteBufCodecs.list()), x -> x.multipliers,
            STARVATION_CODEC.apply(ByteBufCodecs.list()), x -> x.starvation,
            FoodConfig::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, FoodConfigSyncPayload> CODEC = CONFIG_CODEC.map(FoodConfigSyncPayload::new, FoodConfigSyncPayload::config);

    public static void init() {
        Edit.init();
        NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, CODEC, (payload, context) -> context.queue(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.getConnection() instanceof FoodConfigHolder holder) {
                holder.setFoodConfig(payload.config());
            }
            if (minecraft.screen instanceof AbstractFoodConfigScreen screen) {
                screen.refresh();
            }
        }));

        PlayerEvent.PLAYER_JOIN.register(player -> {
            MinecraftServer server = player.getServer();

            if (server != null) {
                NetworkManager.sendToPlayer(player, of(FoodConfigSavedData.get(server)));
            }
        });
    }

    public static FoodConfigSyncPayload of(FoodConfigHolder holder) {
        return new FoodConfigSyncPayload(holder.getFoodConfig());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Edit(FoodConfig config) implements CustomPacketPayload {

        private static final Type<Edit> TYPE = new Type<>(RenourishedDelightMod.key("food_config_edit"));
        private static final StreamCodec<RegistryFriendlyByteBuf, Edit> CODEC = CONFIG_CODEC.map(Edit::new, Edit::config);

        public static void init() {
            NetworkManager.registerReceiver(NetworkManager.c2s(), Edit.TYPE, Edit.CODEC, (payload, context) -> context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player && player.hasPermissions(2)) {
                    MinecraftServer server = player.getServer();

                    if (server != null) {
                        FoodConfigSavedData config = FoodConfigSavedData.get(server);
                        config.setFoodConfig(payload.config());
                        NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), of(config));
                    }
                }
            }));
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
