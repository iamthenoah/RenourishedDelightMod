package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SuppressHurtFlashPayload() implements ModPayload {

    private static final ResourceLocation ID = RenourishedDelightMod.key("suppress_hurt_flash");

    private static final long SUPPRESS_WINDOW_MILLIS = 500L;

    private static long shrinkTimestamp = -1L;

    public static void init() {
        if (Platform.getEnvironment() != Env.CLIENT) return;
        NetworkManager.registerReceiver(NetworkManager.s2c(), ID, (buffer, context) -> context.queue(() -> shrinkTimestamp = System.currentTimeMillis()));
    }

    public static boolean isSuppressed() {
        return shrinkTimestamp >= 0 && System.currentTimeMillis() - shrinkTimestamp <= SUPPRESS_WINDOW_MILLIS;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
    }
}
