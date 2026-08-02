package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FoodConfigEditPayload(List<FoodItemEntry> entries) implements CustomPacketPayload {

    private static final Type<FoodConfigEditPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "food_config_edit"));
    private static final StreamCodec<RegistryFriendlyByteBuf, FoodConfigEditPayload> CODEC = StreamCodec.composite(FoodConfigCodecs.ENTRY_LIST_CODEC, FoodConfigEditPayload::entries, FoodConfigEditPayload::new);

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), TYPE, CODEC, (payload, context) -> context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player && player.hasPermissions(2)) {
                MinecraftServer server = player.getServer();
                
                if (server != null) {
                    FoodConfigHolder config = FoodConfigSavedData.get(server);
                    config.setFoodConfig(payload.entries());
                    FoodConfigSavedData.markDirty(server);
                    FoodConfigSyncPayload.broadcast(server, config.getFoodConfig());
                }
            }
        }));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
