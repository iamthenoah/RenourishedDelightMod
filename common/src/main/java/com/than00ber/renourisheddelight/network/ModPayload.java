package com.than00ber.renourisheddelight.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface ModPayload {

    ResourceLocation id();

    void write(FriendlyByteBuf buffer);

    default void sendTo(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, id(), encode());
    }

    default void sendTo(Iterable<ServerPlayer> players) {
        NetworkManager.sendToPlayers(players, id(), encode());
    }

    default void sendToServer() {
        NetworkManager.sendToServer(id(), encode());
    }

    private FriendlyByteBuf encode() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        write(buffer);
        return buffer;
    }
}
