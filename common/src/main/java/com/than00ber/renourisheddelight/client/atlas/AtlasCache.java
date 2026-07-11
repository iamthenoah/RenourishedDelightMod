package com.than00ber.renourisheddelight.client.atlas;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class AtlasCache {

    private static final int CACHE_FORMAT_VERSION = 2;
    private static final Gson GSON = new Gson();

    public static Path cacheDir() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve(RenourishedDelightMod.MOD_ID)
                .resolve("cache");
    }

    public static @Nullable TextureAtlas tryLoad(Path dir, String name, int dimensions, List<String> packIds, int itemCount) {
        Path imagePath = dir.resolve(name + ".png");
        Path metaPath = dir.resolve(name + ".json");

        if (Files.isRegularFile(imagePath) && Files.isRegularFile(metaPath)) {
            try (InputStream in = Files.newInputStream(imagePath)) {
                NativeImage image = NativeImage.read(in);
                AtlasMeta meta = GSON.fromJson(Files.readString(metaPath), AtlasMeta.class);
                String mismatch = describeMismatch(meta, dimensions, packIds, itemCount);

                if (mismatch == null) {
                    DynamicTexture texture = new DynamicTexture(image);
                    ResourceLocation location = Minecraft.getInstance().getTextureManager().register(name, texture);
                    Map<Item, Texture[]> textures = new HashMap<>();

                    for (Map.Entry<String, int[][]> entry : meta.items.entrySet()) {
                        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.getKey()));
                        if (item == Items.AIR) continue;
                        int[][] coords = entry.getValue();
                        Texture[] slots = new Texture[coords.length];

                        for (int i = 0; i < coords.length; i++) {
                            if (coords[i] != null) {
                                slots[i] = new Texture(location, texture, coords[i][0], coords[i][1], dimensions);
                            }
                        }
                        textures.put(item, slots);
                    }
                    return new TextureAtlas(textures);
                } else {
                    image.close();
                    System.out.printf("[RenourishedDelight] Atlas cache miss for '%s': %s%n", name, mismatch);
                }
            } catch (Exception exception) {
                System.out.printf("[RenourishedDelight] Atlas cache miss for '%s': failed to read cache (%s)%n", name, exception);
            }
        }
        return null;
    }

    private static @Nullable String describeMismatch(@Nullable AtlasMeta meta, int dimensions, List<String> packIds, int itemCount) {
        if (meta == null) return "no cache metadata found";
        if (meta.items == null) return "cache metadata has no items";
        if (meta.formatVersion != CACHE_FORMAT_VERSION) return "format version changed (" + meta.formatVersion + " -> " + CACHE_FORMAT_VERSION + ")";
        if (meta.dimensions != dimensions) return "dimensions changed (" + meta.dimensions + " -> " + dimensions + ")";
        if (meta.itemCount != itemCount) return "item count changed (" + meta.itemCount + " -> " + itemCount + ")";
        if (meta.packIds == null || !meta.packIds.equals(packIds)) return "resource pack list changed (" + meta.packIds + " -> " + packIds + ")";
        return null;
    }

    public static void save(Path dir, String name, TextureAtlas.Builder builder, List<String> packIds, int itemCount) {
        try {
            Files.createDirectories(dir);
            NativeImage pixels = Objects.requireNonNull(builder.texture.getPixels());
            pixels.writeToFile(dir.resolve(name + ".png"));

            AtlasMeta meta = new AtlasMeta();
            meta.formatVersion = CACHE_FORMAT_VERSION;
            meta.dimensions = builder.dimensions;
            meta.packIds = packIds;
            meta.itemCount = itemCount;
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
        } catch (IOException exception) {
            System.out.printf("[RenourishedDelight] Failed to save atlas cache for '%s': %s%n", name, exception);
        }
    }

    private static class AtlasMeta {
        int formatVersion;
        int dimensions;
        int itemCount;
        List<String> packIds;
        Map<String, int[][]> items;
    }
}
