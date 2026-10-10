package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record NoSprintPayload(boolean canSprintValue) implements ModPayload {

    public static final ResourceLocation ID = RenourishedDelightMod.key("can_sprint");

    private static boolean canSprint = true;

    public static void init() {
        if (Platform.getEnvironment() != Env.CLIENT) return;
        NetworkManager.registerReceiver(NetworkManager.s2c(), ID, (buffer, context) -> {
            boolean value = buffer.readBoolean();
            context.queue(() -> canSprint = value);
        });
    }

    public static boolean canSprint() {
        return canSprint;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(canSprintValue);
    }
}
