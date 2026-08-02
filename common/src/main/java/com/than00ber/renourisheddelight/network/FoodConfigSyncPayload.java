package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.compat.client.FoodItemConfigScreen;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FoodConfigSyncPayload(List<FoodItemEntry> entries) implements CustomPacketPayload {

    private static final StreamCodec<RegistryFriendlyByteBuf, AttributeBonus> BONUS_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, bonus -> bonus.attribute,
            ByteBufCodecs.STRING_UTF8, bonus -> bonus.operation,
            ByteBufCodecs.DOUBLE, bonus -> bonus.amount,
            ByteBufCodecs.VAR_INT, bonus -> bonus.duration,
            AttributeBonus::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, FoodItemEntry> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, entry -> entry.item,
            BONUS_CODEC.apply(ByteBufCodecs.list()), entry -> entry.attributes,
            ByteBufCodecs.BOOL, entry -> entry.override,
            FoodItemEntry::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, List<FoodItemEntry>> ENTRIES_CODEC = ENTRY_CODEC.apply(ByteBufCodecs.list());
    private static final StreamCodec<RegistryFriendlyByteBuf, FoodConfigSyncPayload> CODEC = StreamCodec.composite(ENTRIES_CODEC, FoodConfigSyncPayload::entries, FoodConfigSyncPayload::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, Edit> EDIT_CODEC = StreamCodec.composite(ENTRIES_CODEC, Edit::entries, Edit::new);
    private static final Type<FoodConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "food_config_sync"));
    private static final Type<Edit> EDIT_TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "food_config_edit"));

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, CODEC, (payload, context) -> context.queue(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.getConnection() instanceof FoodConfigHolder holder) {
                holder.setFoodConfig(payload.entries());
            }
            if (minecraft.screen instanceof FoodItemConfigScreen screen) {
                screen.refresh();
            }
        }));

        NetworkManager.registerReceiver(NetworkManager.c2s(), EDIT_TYPE, EDIT_CODEC, (payload, context) -> context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player && player.hasPermissions(2)) {
                MinecraftServer server = player.getServer();

                if (server != null) {
                    FoodConfigSavedData config = FoodConfigSavedData.get(server);
                    config.setFoodConfig(payload.entries());
                    broadcast(server, config.getFoodConfig());
                }
            }
        }));

        PlayerEvent.PLAYER_JOIN.register(player -> {
            MinecraftServer server = player.getServer();

            if (server != null) {
                NetworkManager.sendToPlayer(player, new FoodConfigSyncPayload(List.copyOf(FoodConfigSavedData.get(server).getFoodConfig())));
            }
        });
    }

    public static void broadcast(MinecraftServer server, List<FoodItemEntry> entries) {
        NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), new FoodConfigSyncPayload(List.copyOf(entries)));
    }

    public static void sendToServer(List<FoodItemEntry> entries) {
        NetworkManager.sendToServer(new Edit(List.copyOf(entries)));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Edit(List<FoodItemEntry> entries) implements CustomPacketPayload {

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return EDIT_TYPE;
        }
    }
}
