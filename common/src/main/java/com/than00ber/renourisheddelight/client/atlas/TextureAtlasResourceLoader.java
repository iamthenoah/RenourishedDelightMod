package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.than00ber.renourisheddelight.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TextureAtlasResourceLoader implements ResourceManagerReloadListener {

    private static final TextureAtlasResourceLoader INSTANCE = new TextureAtlasResourceLoader();

    public static TextureAtlasResourceLoader getInstance() {
        return INSTANCE;
    }

    private @Nullable TextureAtlas miniAtlas;
    private @Nullable TextureAtlas largeAtlas;

    public @Nullable TextureAtlas getMiniAtlas() {
        return miniAtlas;
    }

    public @Nullable TextureAtlas getLargeAtlas() {
        return largeAtlas;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        Minecraft.getInstance().executeBlocking(() -> {
            long startNanos = System.nanoTime();
            boolean cacheHit = false;

            try {
                List<Item> items = new ArrayList<>(BuiltInRegistries.ITEM.stream()
                        .filter(item -> item.components().has(DataComponents.FOOD))
                        .toList());
                BuiltInRegistries.BLOCK.forEach(x -> items.add(x.asItem()));
                boolean cacheEnabled = Configuration.Client.getInstance().enableAtlasCache;
                Path cacheDir = null;
                List<String> packIds = null;

                if (cacheEnabled) {
                    packIds = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream()
                            .map(Pack::getId)
                            .sorted()
                            .toList();
                    cacheDir = AtlasCache.cacheDir();
                    TextureAtlas cachedMini = AtlasCache.tryLoad(cacheDir, "mini", 9, packIds, items.size());
                    TextureAtlas cachedLarge = AtlasCache.tryLoad(cacheDir, "large", 18, packIds, items.size());

                    if (cachedMini != null && cachedLarge != null) {
                        miniAtlas = cachedMini;
                        largeAtlas = cachedLarge;
                        cacheHit = true;
                        return;
                    }
                }
                TextureAtlas.Builder miniBuilder = new TextureAtlas.Builder("mini", 9, items.size());
                TextureAtlas.Builder largeBuilder = new TextureAtlas.Builder("large", 18, items.size());
                int[] colorPalette = getColorPalette(getGoldenPaletteItem());

                for (Item item : items) {
                    NativeImage baseMini = itemToNativeImage(item, 9);
                    NativeImage baseLarge = itemToNativeImage(item, 18);

                    if (baseMini != null) {
                        miniBuilder.appendTexture(0, item, baseMini)
                                .appendTexture(1, item, makeHunger(baseMini))
                                .appendTexture(2, item, makeSilhouette(baseMini))
                                .appendTexture(3, item, makeOutlined(baseMini))
                                .appendTexture(4, item, makeGolden(baseMini, colorPalette));
                    }
                    if (baseLarge != null) {
                        largeBuilder.appendTexture(0, item, baseLarge)
                                .appendTexture(1, item, makeHunger(baseLarge))
                                .appendTexture(2, item, makeSilhouette(baseLarge))
                                .appendTexture(3, item, makeOutlined(baseLarge))
                                .appendTexture(4, item, makeGolden(baseLarge, colorPalette));
                    }
                }
                miniAtlas = miniBuilder.done();
                largeAtlas = largeBuilder.done();

                if (cacheEnabled) {
                    AtlasCache.save(cacheDir, "mini", miniBuilder, packIds, items.size());
                    AtlasCache.save(cacheDir, "large", largeBuilder, packIds, items.size());
                }
            } catch (Exception exception) {
                // silent fail
            } finally {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
                System.out.printf("[RenourishedDelight] Item icon atlas %s in %d ms%n",
                        cacheHit ? "loaded from cache" : "generated", elapsedMs);
            }
        });
    }

    private @Nullable NativeImage itemToNativeImage(Item item, int dimensions) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ItemStack stack = new ItemStack(item);

        RenderTarget target = new MainTarget(dimensions, dimensions);
        target.setClearColor(0f, 0f, 0f, 0f);
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(true);

        RenderSystem.backupProjectionMatrix();
        Matrix4f projection = new Matrix4f().setOrtho(0, 16, 16, 0, -1000, 1000);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(8f, 8f, 150f); // 150 matches GuiGraphics.renderItem z depth
        poseStack.scale(1f, -1f, 1f);      // flip Y to match screen coords
        poseStack.scale(16f, 16f, 16f);    // scale to fill the 16x16 space

        Lighting.setupForFlatItems();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        BakedModel model = itemRenderer.getModel(stack, null, null, 0);

        try {
            itemRenderer.render(stack, ItemDisplayContext.GUI, false, poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
            bufferSource.endBatch();
            poseStack.popPose();
            Lighting.setupFor3DItems();
            RenderSystem.restoreProjectionMatrix();

            NativeImage image = new NativeImage(dimensions, dimensions, false);
            target.bindRead();
            image.downloadTexture(0, false);
            image.flipY();
            minecraft.getMainRenderTarget().bindWrite(false);
            target.destroyBuffers();
            return image;
        } catch (Exception exception) {
            poseStack.popPose();
            Lighting.setupFor3DItems();
            RenderSystem.restoreProjectionMatrix();
            target.destroyBuffers();
            return null;
        }
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
                    boolean neighbor = x > 0 && ((input.getPixelRGBA(x - 1, y) >> 24) & 0xFF) != 0;
                    if (!neighbor && x < width - 1 && ((input.getPixelRGBA(x + 1, y) >> 24) & 0xFF) != 0) neighbor = true;
                    if (!neighbor && y > 0 && ((input.getPixelRGBA(x, y - 1) >> 24) & 0xFF) != 0) neighbor = true;
                    if (!neighbor && y < height - 1 && ((input.getPixelRGBA(x, y + 1) >> 24) & 0xFF) != 0) neighbor = true;
                    if (neighbor) output.setPixelRGBA(x, y, 0xFFFFFFFF);
                } else if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    output.setPixelRGBA(x, y, 0xFFFFFFFF);
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

    private NativeImage makeGolden(NativeImage input, int[] palette) {
        int width = input.getWidth();
        int height = input.getHeight();
        NativeImage output = new NativeImage(width, height, true);

        if (palette == null || palette.length == 0) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    output.setPixelRGBA(x, y, input.getPixelRGBA(x, y));
                }
            }
            return output;
        }
        float minBrightness = 1f, maxBrightness = 0f;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = input.getPixelRGBA(x, y);
                int a = (pixel >> 24) & 0xFF;

                if (a != 0) {
                    float brightness = brightness(pixel);
                    minBrightness = Math.min(minBrightness, brightness);
                    maxBrightness = Math.max(maxBrightness, brightness);
                }
            }
        }
        float range = Math.max(0.01f, maxBrightness - minBrightness);
        Integer[] boxedPalette = new Integer[palette.length];

        for (int i = 0; i < palette.length; i++) {
            boxedPalette[i] = palette[i];
        }
        Arrays.sort(boxedPalette, (p1, p2) -> Float.compare(brightness(p1), brightness(p2)));

        int[] sortedPalette = new int[boxedPalette.length];

        for (int i = 0; i < boxedPalette.length; i++) {
            sortedPalette[i] = boxedPalette[i];
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = input.getPixelRGBA(x, y);
                int a = (pixel >> 24) & 0xFF;

                if (a != 0) {
                    float brightness = brightness(pixel);
                    float t = (brightness - minBrightness) / range;
                    t = (float) Math.pow(t, 0.7);
                    int index = Mth.clamp((int) (t * (sortedPalette.length - 1)), 0, sortedPalette.length - 1);
                    int goldPixel = sortedPalette[index];
                    int r = (goldPixel >> 16) & 0xFF;
                    int g = (goldPixel >> 8) & 0xFF;
                    int b = goldPixel & 0xFF;
                    output.setPixelRGBA(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                } else {
                    output.setPixelRGBA(x, y, 0x00000000);
                }
            }
        }
        return output;
    }
    
    private int[] getColorPalette(Item item) {
        NativeImage image = itemToNativeImage(item, 16);
        if (image == null) return null;

        int width = image.getWidth();
        int height = image.getHeight();
        List<Integer> opaquePixels = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = image.getPixelRGBA(x, y);
                int a = (pixel >> 24) & 0xFF;
                
                if (a != 0) {
                    opaquePixels.add(pixel);
                }
            }
        }
        if (opaquePixels.isEmpty()) return null;
        opaquePixels.sort(Comparator.comparingInt(TextureAtlasResourceLoader::luminance));
        int[] palette = new int[opaquePixels.size()];

        for (int i = 0; i < palette.length; i++) {
            palette[i] = opaquePixels.get(i);
        }
        return palette;
    }

    private Item getGoldenPaletteItem() {
        try {
            String name = Configuration.Client.getInstance().goldenPaletteItem;
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(name));
            return item != Items.AIR ? item : Items.GOLDEN_CARROT;
        } catch (Exception exception) {
            return Items.GOLDEN_CARROT;
        }
    }
    
    private static int luminance(int pixel) {
        return (((pixel >> 16) & 0xFF) * 299 + ((pixel >> 8) & 0xFF) * 587 + (pixel & 0xFF) * 114) / 1000;
    }

    private static float brightness(int pixel) {
        return Color.RGBtoHSB((pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF, null)[2];
    }
}