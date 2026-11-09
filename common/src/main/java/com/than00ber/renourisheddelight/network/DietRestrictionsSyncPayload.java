package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.food.DietRestrictions;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record DietRestrictionsSyncPayload(Map<ResourceLocation, List<ResourceLocation>> restrictions) implements ModPayload {

    private static final ResourceLocation ID = RenourishedDelightMod.key("diet_restrictions_sync");

    public static void init() {
        if (Platform.getEnvironment() == Env.CLIENT) initClient();

        // tags and recipes are only final once a (re)load has fully finished, so resolve on the next tick
        TickEvent.SERVER_POST.register(server -> {
            if (DietRestrictions.server().isDirty()) {
                DietRestrictions.server().rebuild(server);
                of(server).sendTo(server.getPlayerList().getPlayers());
            }
        });
        LifecycleEvent.SERVER_STOPPED.register(server -> DietRestrictions.server().onUpdateTags());

        PlayerEvent.PLAYER_JOIN.register(player -> {
            MinecraftServer server = player.getServer();
            if (server != null) of(server).sendTo(player);
        });
    }

    private static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), ID, (buffer, context) -> {
            DietRestrictionsSyncPayload payload = read(buffer);

            context.queue(() -> {
                Map<Item, Set<ResourceLocation>> restrictions = new HashMap<>();

                payload.restrictions().forEach((id, tags) -> {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    if (item != Items.AIR) restrictions.put(item, Set.copyOf(tags));
                });
                DietRestrictions.client().set(restrictions);
            });
        });
    }

    public static DietRestrictionsSyncPayload of(MinecraftServer server) {
        DietRestrictions restrictions = DietRestrictions.server();
        if (restrictions.isDirty()) restrictions.rebuild(server);
        Map<ResourceLocation, List<ResourceLocation>> encoded = new HashMap<>();
        restrictions.all().forEach((item, tags) -> encoded.put(BuiltInRegistries.ITEM.getKey(item), List.copyOf(tags)));
        return new DietRestrictionsSyncPayload(encoded);
    }

    private static DietRestrictionsSyncPayload read(FriendlyByteBuf buffer) {
        return new DietRestrictionsSyncPayload(buffer.readMap(FriendlyByteBuf::readResourceLocation, in -> in.readList(FriendlyByteBuf::readResourceLocation)));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeMap(restrictions, FriendlyByteBuf::writeResourceLocation, (out, tags) -> out.writeCollection(tags, FriendlyByteBuf::writeResourceLocation));
    }
}
