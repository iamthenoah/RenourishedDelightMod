package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.compat.client.food.AbstractFoodConfigScreen;
import com.than00ber.renourisheddelight.config.data.*;
import com.than00ber.renourisheddelight.data.level.FoodConfigSavedData;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record FoodConfigSyncPayload(FoodConfig config, List<FoodItemEntry> presets) implements ModPayload {

    private static final ResourceLocation ID = RenourishedDelightMod.key("food_config_sync");

    public static void init() {
        Edit.init();
        if (Platform.getEnvironment() == Env.CLIENT) initClient();

        PlayerEvent.PLAYER_JOIN.register(player -> {
            MinecraftServer server = player.getServer();

            if (server != null) {
                of(FoodConfigSavedData.get(server)).sendTo(player);
            }
        });
    }

    private static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), ID, (buffer, context) -> {
            FoodConfigSyncPayload payload = read(buffer);

            context.queue(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.getConnection() instanceof FoodConfigHolder holder) {
                    holder.sync(payload.config(), payload.presets());
                }
                if (minecraft.screen instanceof AbstractFoodConfigScreen screen) {
                    screen.refresh();
                }
            });
        });
    }

    public static FoodConfigSyncPayload of(FoodConfigHolder holder) {
        return new FoodConfigSyncPayload(holder.getFoodConfig(), holder.getPresets());
    }

    private static FoodConfigSyncPayload read(FriendlyByteBuf buffer) {
        return new FoodConfigSyncPayload(readConfig(buffer), buffer.readList(FoodConfigSyncPayload::readEntry));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        writeConfig(buffer, config);
        buffer.writeCollection(presets, FoodConfigSyncPayload::writeEntry);
    }

    private static void writeConfig(FriendlyByteBuf buffer, FoodConfig config) {
        buffer.writeCollection(config.foods, FoodConfigSyncPayload::writeEntry);
        buffer.writeCollection(config.multipliers, (out, x) -> {
            out.writeUtf(x.attribute);
            out.writeDouble(x.multiplier);
        });
        buffer.writeCollection(config.starvation, (out, x) -> {
            out.writeUtf(x.effect);
            out.writeVarInt(x.after);
            out.writeVarInt(x.amplifier);
            out.writeVarInt(x.max);
        });
    }

    private static FoodConfig readConfig(FriendlyByteBuf buffer) {
        List<FoodItemEntry> foods = buffer.readList(FoodConfigSyncPayload::readEntry);
        List<DurationMultiplierEntry> multipliers = buffer.readList(in -> new DurationMultiplierEntry(in.readUtf(), in.readDouble()));
        List<StarvationEntry> starvation = buffer.readList(in -> new StarvationEntry(in.readUtf(), in.readVarInt(), in.readVarInt(), in.readVarInt()));
        return new FoodConfig(foods, multipliers, starvation);
    }

    private static void writeEntry(FriendlyByteBuf buffer, FoodItemEntry entry) {
        buffer.writeUtf(entry.item);
        buffer.writeCollection(entry.attributes, (out, x) -> {
            out.writeUtf(x.attribute);
            out.writeUtf(x.operation);
            out.writeDouble(x.amount);
            out.writeVarInt(x.duration);
        });
    }

    private static FoodItemEntry readEntry(FriendlyByteBuf buffer) {
        return new FoodItemEntry(buffer.readUtf(), buffer.readList(in -> new AttributeBonus(in.readUtf(), in.readUtf(), in.readDouble(), in.readVarInt())));
    }

    public record Edit(FoodConfig config) implements ModPayload {

        private static final ResourceLocation ID = RenourishedDelightMod.key("food_config_edit");

        public static void init() {
            NetworkManager.registerReceiver(NetworkManager.c2s(), ID, (buffer, context) -> {
                FoodConfig config = readConfig(buffer);

                context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player && player.hasPermissions(2)) {
                        MinecraftServer server = player.getServer();

                        if (server != null) {
                            FoodConfigSavedData saved = FoodConfigSavedData.get(server);
                            saved.setFoodConfig(config);
                            of(saved).sendTo(server.getPlayerList().getPlayers());
                        }
                    }
                });
            });
        }

        @Override
        public ResourceLocation id() {
            return ID;
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            writeConfig(buffer, config);
        }
    }
}
