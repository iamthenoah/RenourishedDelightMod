package com.than00ber.renourisheddelight.client.atlas;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TextureAtlasResourceLoader implements ResourceManagerReloadListener {

    private static final TextureAtlasResourceLoader INSTANCE = new TextureAtlasResourceLoader();
    private static final int MINI_SIZE = 9;
    private static final int LARGE_SIZE = 18;

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
        long startNanos = System.nanoTime();

        try {
            Set<Item> items = new LinkedHashSet<>();
            BuiltInRegistries.ITEM.stream().filter(x -> x.components().has(DataComponents.FOOD)).forEach(items::add);
            BuiltInRegistries.BLOCK.forEach(x -> items.add(x.asItem()));
            items.remove(Items.AIR);
            TextureAtlas.Builder miniBuilder = new TextureAtlas.Builder("mini", MINI_SIZE, items.size());
            TextureAtlas.Builder largeBuilder = new TextureAtlas.Builder("large", LARGE_SIZE, items.size());
            int[] palette = getColorPalette(getGoldenPaletteItem());

            for (Item item : items) {
                appendItem(miniBuilder, item, MINI_SIZE, palette);
                appendItem(largeBuilder, item, LARGE_SIZE, palette);
            }
            TextureAtlas mini = miniBuilder.done();
            TextureAtlas large = largeBuilder.done();

            if (mini.textures().isEmpty() || large.textures().isEmpty()) {
                mini.release();
                large.release();
                throw new IllegalStateException("no item textures could be rendered");
            }
            if (miniAtlas != null) miniAtlas.release();
            if (largeAtlas != null) largeAtlas.release();
            miniAtlas = mini;
            largeAtlas = large;
            RenourishedDelightMod.LOGGER.info("Item icon atlas generated in {} ms", (System.nanoTime() - startNanos) / 1_000_000L);
        } catch (Exception exception) {
            RenourishedDelightMod.LOGGER.error("Failed to generate item icon atlas", exception);
        }
    }

    private void appendItem(TextureAtlas.Builder builder, Item item, int dimensions, int @Nullable [] palette) {
        try (NativeImage base = itemToNativeImage(item, dimensions)) {
            if (base != null) {
                try (NativeImage hunger = makeHunger(base); 
                     NativeImage silhouette = makeSilhouette(base);
                     NativeImage outlined = makeOutlined(base);
                     NativeImage golden = makeGolden(base, palette)) {
                    builder.appendTexture(0, item, base)
                            .appendTexture(1, item, hunger)
                            .appendTexture(2, item, silhouette)
                            .appendTexture(3, item, outlined)
                            .appendTexture(4, item, golden);
                }
            }
        }
    }

    private @Nullable NativeImage itemToNativeImage(Item item, int dimensions) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ItemStack stack = new ItemStack(item);

        RenderTarget target = new MainTarget(dimensions, dimensions);
        target.setClearColor(0F, 0F, 0F, 0F);
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(true);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, 16, 16, 0, -1000, 1000), VertexSorting.ORTHOGRAPHIC_Z);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(8F, 8F, 150F); // 150 matches GuiGraphics.renderItem z depth
        poseStack.scale(1F, -1F, 1F);      // flip Y to match screen coords
        poseStack.scale(16F, 16F, 16F);    // scale to fill the 16x16 space
        Lighting.setupForFlatItems();

        try {
            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            BakedModel model = itemRenderer.getModel(stack, null, null, 0);
            itemRenderer.render(stack, ItemDisplayContext.GUI, false, poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
            bufferSource.endBatch();

            NativeImage image = new NativeImage(dimensions, dimensions, false);
            target.bindRead();
            image.downloadTexture(0, false);
            image.flipY();
            return image;
        } catch (Exception exception) {
            return null;
        } finally {
            poseStack.popPose();
            Lighting.setupFor3DItems();
            RenderSystem.restoreProjectionMatrix();
            minecraft.getMainRenderTarget().bindWrite(false);
            target.destroyBuffers();
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

    private NativeImage makeGolden(NativeImage input, int @Nullable [] palette) {
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
        float minBrightness = 1F;
        float maxBrightness = 0F;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = input.getPixelRGBA(x, y);

                if (((pixel >> 24) & 0xFF) != 0) {
                    float brightness = brightness(pixel);
                    minBrightness = Math.min(minBrightness, brightness);
                    maxBrightness = Math.max(maxBrightness, brightness);
                }
            }
        }
        float range = Math.max(0.01F, maxBrightness - minBrightness);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = input.getPixelRGBA(x, y);
                int a = (pixel >> 24) & 0xFF;

                if (a != 0) {
                    float t = (float) Math.pow((brightness(pixel) - minBrightness) / range, 0.7);
                    int index = Mth.clamp((int) (t * (palette.length - 1)), 0, palette.length - 1);
                    output.setPixelRGBA(x, y, (a << 24) | (palette[index] & 0x00FFFFFF));
                } else {
                    output.setPixelRGBA(x, y, 0x00000000);
                }
            }
        }
        return output;
    }

    private int @Nullable [] getColorPalette(Item item) {
        try (NativeImage image = itemToNativeImage(item, 16)) {
            if (image != null) {
                List<Integer> pixels = new ArrayList<>();

                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        int pixel = image.getPixelRGBA(x, y);
                        if (((pixel >> 24) & 0xFF) != 0) pixels.add(pixel);
                    }
                }
                if (pixels.isEmpty()) return null;
                pixels.sort((p1, p2) -> Float.compare(brightness(p1), brightness(p2)));
                int[] palette = new int[pixels.size()];

                for (int i = 0; i < palette.length; i++) {
                    palette[i] = pixels.get(i);
                }
                return palette;
            }
        }
        return null;
    }

    private Item getGoldenPaletteItem() {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(ClientConfiguration.getInstance().goldenPaletteItem));
            return item != Items.AIR ? item : Items.GOLDEN_CARROT;
        } catch (Exception exception) {
            return Items.GOLDEN_CARROT;
        }
    }

    private static float brightness(int pixel) {
        return Math.max((pixel >> 16) & 0xFF, Math.max((pixel >> 8) & 0xFF, pixel & 0xFF)) / 255F;
    }
}
