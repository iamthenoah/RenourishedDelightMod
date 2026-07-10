package com.than00ber.renourisheddelight.client.atlas;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import com.than00ber.renourisheddelight.Configuration;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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

/**
 * On-disk cache for generated {@link TextureAtlas} images.
 * <p>
 * Rendering every item's icon (twice, at 9x9 and 18x18) is the expensive part of a resource
 * reload, and its output only depends on: which resource packs are active and in what order,
 * which items exist (i.e. which mods are loaded), and the golden-palette config. This class
 * hashes those three inputs into a cache key, and stores/loads the resulting atlas image + a
 * small JSON sidecar describing where each item's icon variants landed in that image.
 */
public final class AtlasCache {

    /**
     * Bump this whenever the pixel content the loader generates changes (new visual variant,
     * different rendering math, etc). A version bump invalidates every existing cache entry
     * even if the resource-pack/item hash would otherwise still match.
     */
    private static final int CACHE_FORMAT_VERSION = 1;

    /** How many distinct resource-pack/item combinations to keep on disk at once. */
    private static final int MAX_CACHED_ENTRIES = 5;

    private static final Gson GSON = new Gson();

    private AtlasCache() {
    }

    /**
     * Root directory all cache entries live under: {@code <game dir>/config/renourisheddelight/atlascache}.
     */
    public static Path cacheRoot() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve(RenourishedDelightMod.MOD_ID)
                .resolve("atlascache");
    }

    public static Path cacheDir(String key) {
        return cacheRoot().resolve(key);
    }

    /**
     * Hashes the active resource pack ids (in load order), the current item set, and the
     * golden-palette config into a stable cache key.
     */
    public static String computeCacheKey(ResourceManager manager, List<Item> items) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, "format:" + CACHE_FORMAT_VERSION);

            manager.listPacks().forEachOrdered(pack -> update(digest, "pack:" + pack.packId()));

            for (Item item : items) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                update(digest, "item:" + key);
            }
            update(digest, "palette:" + Configuration.Client.getInstance().goldenPaletteItem);

            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    /**
     * Attempts to load a previously cached atlas. Returns {@code null} on any miss or failure
     * (missing files, format mismatch, corrupt data) so the caller can fall back to regenerating.
     */
    public static @Nullable TextureAtlas tryLoad(Path dir, String name, int dimensions) {
        Path imagePath = dir.resolve(name + ".png");
        Path metaPath = dir.resolve(name + ".json");

        if (!Files.isRegularFile(imagePath) || !Files.isRegularFile(metaPath)) {
            return null;
        }

        try (InputStream in = Files.newInputStream(imagePath)) {
            NativeImage image = NativeImage.read(in);
            AtlasMeta meta = GSON.fromJson(Files.readString(metaPath), AtlasMeta.class);

            if (meta == null || meta.formatVersion != CACHE_FORMAT_VERSION || meta.dimensions != dimensions || meta.items == null) {
                image.close();
                return null;
            }

            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation location = Minecraft.getInstance().getTextureManager().register(name, texture);
            AtlasHandle handle = new AtlasHandle(location, texture);

            Map<Item, Texture[]> textures = new HashMap<>();
            for (Map.Entry<String, int[][]> entry : meta.items.entrySet()) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.getKey()));
                if (item == Items.AIR) {
                    continue; // item no longer exists (mod/resource change slipped past the hash) - skip it
                }
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
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Writes a freshly-built atlas (image + item->coordinate metadata) to disk, then trims the
     * cache down to {@link #MAX_CACHED_ENTRIES} most-recently-used entries.
     */
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
            evictOldEntries(dir.getParent());
        } catch (IOException e) {
            // Non-fatal: failing to write the cache just means we regenerate next reload.
        }
    }

    private static void evictOldEntries(Path root) {
        try {
            if (!Files.isDirectory(root)) {
                return;
            }
            List<Path> entries;
            try (var stream = Files.list(root)) {
                entries = stream.filter(Files::isDirectory)
                        .sorted(Comparator.comparingLong(AtlasCache::lastModifiedMillis).reversed())
                        .toList();
            }
            for (int i = MAX_CACHED_ENTRIES; i < entries.size(); i++) {
                deleteRecursive(entries.get(i));
            }
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }

    private static long lastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }

    private static void deleteRecursive(Path path) {
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private static class AtlasMeta {
        int formatVersion;
        int dimensions;
        Map<String, int[][]> items;
    }
}
