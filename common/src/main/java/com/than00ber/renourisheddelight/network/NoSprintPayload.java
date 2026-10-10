package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record NoSprintPayload(boolean canSprintValue) implements CustomPacketPayload {

    public static final Type<NoSprintPayload> TYPE = new Type<>(
            RenourishedDelightMod.key("can_sprint"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NoSprintPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            NoSprintPayload::canSprintValue,
            NoSprintPayload::new);

    private static boolean canSprint = true;

    public static boolean canSprint() {
        return canSprint;
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, CODEC,
                (payload, context) -> context.queue(() -> {
                    canSprint = payload.canSprintValue();
                }));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
