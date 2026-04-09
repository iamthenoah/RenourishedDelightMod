package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MiniTextureAtlasResourceLoader implements ResourceManagerReloadListener {

    private static final MiniTextureAtlasResourceLoader INSTANCE = new MiniTextureAtlasResourceLoader();

    public static MiniTextureAtlasResourceLoader getInstance() {
        return INSTANCE;
    }

    public static void init() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, MiniTextureAtlasResourceLoader.getInstance());
    }

    private MiniTextureAtlas atlas;

    public @Nullable MiniTextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        try {
            List<Item> items = new ArrayList<>(BuiltInRegistries.ITEM.stream()
                    .filter(Item::isEdible)
                    .toList());
            BuiltInRegistries.BLOCK.forEach(x -> items.add(x.asItem()));
            MiniTextureAtlas.Builder builder = new MiniTextureAtlas.Builder(items.size());

            for (Item item : items) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                String name = "textures/item/" + key.getPath() + ".png";
                ResourceLocation path = new ResourceLocation(key.getNamespace(), name);
                Optional<Resource> resource = manager.getResource(path);

                if (resource.isPresent()) {
                    try (InputStream stream = resource.get().open()) {
                        NativeImage base = makeBase(NativeImage.read(stream));
                        builder.appendTexture(0, item, base)
                                .appendTexture(1, item, makeHunger(base))
                                .appendTexture(2, item, makeSilhouette(base))
                                .appendTexture(3, item, makeOutlined(base));
                    }
                }
            }
            atlas = builder.done();
        } catch (Exception exception) {
            // do nothing
        }
    }

    private NativeImage makeBase(NativeImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        NativeImage output = new NativeImage(MiniTexture.DIMENSIONS, MiniTexture.DIMENSIONS, true);

        for (int x = 0; x < width / 2; x++) {
            for (int y = 0; y < height / 2; y++) {
                output.setPixelRGBA(x, y, input.getPixelRGBA(x * (width / 8), y * (height / 8)));
            }
        }
        return output;
    }

    private NativeImage makeHunger(NativeImage base) {
        int width = base.getWidth();
        int height = base.getHeight();
        NativeImage output = new NativeImage(width, height, true);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = base.getPixelRGBA(x, y);
                int a = (pixel >> 24) & 0xFF;

                if (a != 0) {
                    int r = (int) (((pixel >> 16) & 0xFF) * (1 - 0.2F) + 60 * 0.2F);
                    int g = (int) (((pixel >> 8) & 0xFF) * (1 - 0.2F) + 120 * 0.2F);
                    int b = (int) ((pixel & 0xFF) * (1 - 0.2F) + 50 * 0.2F);
                    output.setPixelRGBA(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                } else {
                    output.setPixelRGBA(x, y, 0x00000000);
                }
            }
        }
        return output;
    }
    
    private NativeImage makeOutlined(NativeImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        NativeImage output = new NativeImage(width, height, true);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int a = (input.getPixelRGBA(x, y) >> 24) & 0xFF;

                if (a == 0) {
                    boolean neighbor = false;

                    if (x > 0) {
                        if (((input.getPixelRGBA(x - 1, y) >> 24) & 0xFF) != 0) neighbor = true;
                    }
                    if (!neighbor && x < width - 1) {
                        if (((input.getPixelRGBA(x + 1, y) >> 24) & 0xFF) != 0) neighbor = true;
                    }
                    if (!neighbor && y > 0) {
                        if (((input.getPixelRGBA(x, y - 1) >> 24) & 0xFF) != 0) neighbor = true;
                    }
                    if (!neighbor && y < height - 1) {
                        if (((input.getPixelRGBA(x, y + 1) >> 24) & 0xFF) != 0) neighbor = true;
                    }
                    if (neighbor) {
                        output.setPixelRGBA(x, y, 0xFFFFFFFF);
                    }
                }
            }
        }
        return output;
    }

    private NativeImage makeSilhouette(NativeImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        NativeImage output = new NativeImage(width, height, true);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int a = (input.getPixelRGBA(x, y) >> 24) & 0xFF;
                output.setPixelRGBA(x, y, a != 0 ? 0xFFFFFFFF : 0x00000000);
            }
        }
        return output;
    }
}
