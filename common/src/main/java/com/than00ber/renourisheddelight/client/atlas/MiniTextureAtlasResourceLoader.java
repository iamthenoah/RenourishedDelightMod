package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MiniTextureAtlasResourceLoader implements ResourceManagerReloadListener {

    private static final MiniTextureAtlasResourceLoader INSTANCE = new MiniTextureAtlasResourceLoader();

    public static MiniTextureAtlasResourceLoader getInstance() {
        return INSTANCE;
    }

    private MiniTextureAtlas atlas;

    public @Nullable MiniTextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.executeBlocking(() -> {
            try {
                List<Item> items = new ArrayList<>(BuiltInRegistries.ITEM.stream()
                        .filter(item -> item.components().has(DataComponents.FOOD))
                        .toList());
                BuiltInRegistries.BLOCK.forEach(x -> items.add(x.asItem()));
                MiniTextureAtlas.Builder builder = new MiniTextureAtlas.Builder(items.size());
                int[] goldenPalette = getGoldenPalette();

                for (Item item : items) {
                    NativeImage base = renderItemToNativeImage(item);

                    if (base != null) {
                        builder.appendTexture(0, item, base)
                                .appendTexture(1, item, makeHunger(base))
                                .appendTexture(2, item, makeSilhouette(base))
                                .appendTexture(3, item, makeOutlined(base))
                                .appendTexture(4, item, makeGolden(base, goldenPalette));
                    }
                }
                atlas = builder.done();
            } catch (Exception e) {
                // silent fail
            }
        });
    }

    private @Nullable NativeImage renderItemToNativeImage(Item item) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ItemStack stack = new ItemStack(item);

        RenderTarget target = new MainTarget(MiniTexture.DIMENSIONS, MiniTexture.DIMENSIONS);
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

            NativeImage image = new NativeImage(MiniTexture.DIMENSIONS, MiniTexture.DIMENSIONS, false);
            target.bindRead();
            image.downloadTexture(0, false);
            image.flipY();
            minecraft.getMainRenderTarget().bindWrite(false);
            target.destroyBuffers();
            return image;
        } catch (Exception e) {
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
    
    private int[] getGoldenPalette() {
        NativeImage carrotImage = renderItemToNativeImage(net.minecraft.world.item.Items.GOLDEN_CARROT);
        if (carrotImage == null) return null;

        int width = carrotImage.getWidth();
        int height = carrotImage.getHeight();
        List<Integer> opaquePixels = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = carrotImage.getPixelRGBA(x, y);
                int a = (pixel >> 24) & 0xFF;
                if (a != 0) {
                    opaquePixels.add(pixel);
                }
            }
        }
        if (opaquePixels.isEmpty()) return null;
        opaquePixels.sort(Comparator.comparingInt(MiniTextureAtlasResourceLoader::luminance));
        int[] palette = new int[opaquePixels.size()];

        for (int i = 0; i < palette.length; i++) {
            palette[i] = opaquePixels.get(i);
        }
        return palette;
    }

    private static int luminance(int pixel) {
        return (((pixel >> 16) & 0xFF) * 299 + ((pixel >> 8) & 0xFF) * 587 + (pixel & 0xFF) * 114) / 1000;
    }

    private static float brightness(int pixel) {
        return Color.RGBtoHSB((pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF, null)[2];
    }
}