package com.than00ber.renourisheddelight.network;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.food.DietRestrictions;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record DietRestrictionsSyncPayload(Map<ResourceLocation, List<ResourceLocation>> restrictions) implements CustomPacketPayload {

    private static final Type<DietRestrictionsSyncPayload> TYPE = new Type<>(RenourishedDelightMod.key("diet_restrictions_sync"));
    private static final StreamCodec<RegistryFriendlyByteBuf, DietRestrictionsSyncPayload> CODEC = ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, List<ResourceLocation>, Map<ResourceLocation, List<ResourceLocation>>>map(
            HashMap::new,
            ResourceLocation.STREAM_CODEC,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())
    ).map(DietRestrictionsSyncPayload::new, DietRestrictionsSyncPayload::restrictions);

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, CODEC, (payload, context) -> context.queue(() -> {
            Map<Item, Set<ResourceLocation>> restrictions = new HashMap<>();

            payload.restrictions().forEach((id, tags) -> {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != Items.AIR) restrictions.put(item, Set.copyOf(tags));
            });
            DietRestrictions.client().set(restrictions);
        }));

        // tags and recipes are only final once a (re)load has fully finished, so resolve on the next tick
        TickEvent.SERVER_POST.register(server -> {
            if (DietRestrictions.server().isDirty()) {
                DietRestrictions.server().rebuild(server);
                NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), of(server));
            }
        });
        LifecycleEvent.SERVER_STOPPED.register(server -> DietRestrictions.server().onUpdateTags());

        PlayerEvent.PLAYER_JOIN.register(player -> {
            MinecraftServer server = player.getServer();
            if (server != null) NetworkManager.sendToPlayer(player, of(server));
        });
    }

    public static DietRestrictionsSyncPayload of(MinecraftServer server) {
        DietRestrictions restrictions = DietRestrictions.server();
        if (restrictions.isDirty()) restrictions.rebuild(server);
        Map<ResourceLocation, List<ResourceLocation>> encoded = new HashMap<>();
        restrictions.all().forEach((item, tags) -> encoded.put(BuiltInRegistries.ITEM.getKey(item), List.copyOf(tags)));
        return new DietRestrictionsSyncPayload(encoded);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
