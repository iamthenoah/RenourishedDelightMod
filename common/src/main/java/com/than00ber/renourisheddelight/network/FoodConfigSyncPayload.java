package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.compat.client.FoodItemConfigScreen;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FoodConfigSyncPayload(List<FoodItemEntry> entries) implements CustomPacketPayload {

    private static final Type<FoodConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "food_config_sync"));
    private static final StreamCodec<RegistryFriendlyByteBuf, FoodConfigSyncPayload> CODEC = StreamCodec.composite(FoodConfigCodecs.ENTRY_LIST_CODEC, FoodConfigSyncPayload::entries, FoodConfigSyncPayload::new);

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, CODEC, (payload, context) -> context.queue(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.level instanceof FoodConfigHolder holder) {
                holder.setFoodConfig(payload.entries());
            }
            if (minecraft.screen instanceof FoodItemConfigScreen screen) {
                screen.refresh();
            }
        }));

        PlayerEvent.PLAYER_JOIN.register(player -> {
            MinecraftServer server = player.getServer();

            if (server != null) {
                List<FoodItemEntry> entries = List.copyOf(FoodConfigSavedData.get(server).getFoodConfig());
                NetworkManager.sendToPlayer(player, new FoodConfigSyncPayload(entries));
            }
        });
    }

    public static void broadcast(MinecraftServer server, List<FoodItemEntry> entries) {
        FoodConfigSyncPayload payload = new FoodConfigSyncPayload(List.copyOf(entries));
        NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), payload);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
