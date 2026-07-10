package com.than00ber.renourisheddelight.client.atlas;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

public final class AtlasCache {

    private static final int CACHE_FORMAT_VERSION = 1;
    private static final int MAX_CACHED_ENTRIES = 5;
    private static final Gson GSON = new Gson();

    public static Path cacheDir(Stream<PackResources> packs, List<Item> items) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, "format:" + CACHE_FORMAT_VERSION);
            packs.forEachOrdered(x -> update(digest, "pack:" + x.packId()));

            for (Item item : items) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                update(digest, "item:" + key);
            }
            update(digest, "palette:" + Configuration.Client.getInstance().goldenPaletteItem);
            return Minecraft.getInstance().gameDirectory.toPath()
                    .resolve("config")
                    .resolve(RenourishedDelightMod.MOD_ID)
                    .resolve("cache")
                    .resolve(HexFormat.of().formatHex(digest.digest()));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    public static @Nullable TextureAtlas tryLoad(Path dir, String name, int dimensions) {
        Path imagePath = dir.resolve(name + ".png");
        Path metaPath = dir.resolve(name + ".json");

        if (Files.isRegularFile(imagePath) && Files.isRegularFile(metaPath)) {
            try (InputStream in = Files.newInputStream(imagePath)) {
                NativeImage image = NativeImage.read(in);
                AtlasMeta meta = GSON.fromJson(Files.readString(metaPath), AtlasMeta.class);

                if (meta != null && meta.formatVersion == CACHE_FORMAT_VERSION && meta.dimensions == dimensions && meta.items != null) {
                    DynamicTexture texture = new DynamicTexture(image);
                    ResourceLocation location = Minecraft.getInstance().getTextureManager().register(name, texture);
                    AtlasHandle handle = new AtlasHandle(location, texture);
                    Map<Item, Texture[]> textures = new HashMap<>();

                    for (Map.Entry<String, int[][]> entry : meta.items.entrySet()) {
                        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.getKey()));
                        if (item == Items.AIR)
                            continue; // item no longer exists (mod/resource change slipped past the hash) - skip it
                        int[][] coords = entry.getValue();
                        Texture[] slots = new Texture[coords.length];

                        for (int i = 0; i < coords.length; i++) {
                            if (coords[i] != null) {
                                slots[i] = new Texture(handle, coords[i][0], coords[i][1], dimensions);
                            }
                        }
                        textures.put(item, slots);
                    }
                    return new TextureAtlas(textures);
                } else {
                    image.close();
                }
            } catch (Exception exception) {
                // do nothing
            }
        }
        return null;
    }

    public static void save(Path dir, String name, TextureAtlas.Builder builder) {
        try {
            Files.createDirectories(dir);
            NativeImage pixels = Objects.requireNonNull(builder.texture.getPixels());
            pixels.writeToFile(dir.resolve(name + ".png"));

            AtlasMeta meta = new AtlasMeta();
            meta.formatVersion = CACHE_FORMAT_VERSION;
            meta.dimensions = builder.dimensions;
            meta.items = new LinkedHashMap<>();

            for (Map.Entry<Item, Texture[]> entry : builder.textures.entrySet()) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getKey());
                Texture[] slots = entry.getValue();
                int[][] coords = new int[slots.length][];

                for (int i = 0; i < slots.length; i++) {
                    coords[i] = slots[i] != null ? new int[]{slots[i].u(), slots[i].v()} : null;
                }
                meta.items.put(key.toString(), coords);
            }
            Files.writeString(dir.resolve(name + ".json"), GSON.toJson(meta));
            Path root = dir.getParent();

            if (Files.isDirectory(root)) {
                List<Path> entries;

                try (Stream<Path> stream = Files.list(root)) {
                    entries = stream.filter(Files::isDirectory)
                            .sorted(Comparator.comparingLong(x -> {
                                try {
                                    return Files.getLastModifiedTime((Path) x).toMillis();
                                } catch (Exception exception) {
                                    // do nothing
                                }
                                return Long.MIN_VALUE;
                            }).reversed())
                            .toList();
                }
                for (int i = MAX_CACHED_ENTRIES; i < entries.size(); i++) {
                    try (Stream<Path> stream = Files.walk(entries.get(i))) {
                        stream.sorted(Comparator.reverseOrder()).forEach(x -> {
                            try {
                                Files.deleteIfExists(x);
                            } catch (IOException exception) {
                                // do nothing
                            }
                        });
                    }
                }
            }
        } catch (IOException exception) {
            // do nothing
        }
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private static class AtlasMeta {
        int formatVersion;
        int dimensions;
        Map<String, int[][]> items;
    }
}
