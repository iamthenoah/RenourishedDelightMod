package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SuppressHurtFlashPayload() implements CustomPacketPayload {

    private static final Type<SuppressHurtFlashPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RenourishedDelightMod.MOD_ID, "suppress_hurt_flash"));
    private static final StreamCodec<RegistryFriendlyByteBuf, SuppressHurtFlashPayload> CODEC = StreamCodec.unit(new SuppressHurtFlashPayload());
    private static final long WINDOW_MILLIS = 500L;

    private static long shrinkTimestamp = -1L;

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, CODEC, (payload, context) -> context.queue(() -> shrinkTimestamp = System.currentTimeMillis()));
    }

    public static boolean isSuppressed() {
        return shrinkTimestamp >= 0 && System.currentTimeMillis() - shrinkTimestamp <= WINDOW_MILLIS;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
