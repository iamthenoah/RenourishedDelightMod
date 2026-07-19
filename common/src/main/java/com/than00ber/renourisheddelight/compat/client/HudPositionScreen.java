package com.than00ber.renourisheddelight.compat.client;

import com.mojang.blaze3d.platform.Window;
import com.than00ber.renourisheddelight.client.overlay.FoodBarOverlay;
import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.DietHolder;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class HudPositionScreen extends Screen {

    private static final Item[] PREVIEW_POOL = {
            Items.BREAD, Items.APPLE, Items.COOKED_BEEF, Items.GOLDEN_CARROT, Items.COOKIE, Items.CARROT
    };

    private final @Nullable Screen parent;
    private final List<ConsumableFoodInstance> previewSlots;
    private final int boxWidth;
    private final int boxHeight = 9;
    private int offsetX;
    private int offsetY;
    private boolean dragging;
    private double dragStartMouseX;
    private double dragStartMouseY;
    private int dragStartOffsetX;
    private int dragStartOffsetY;

    public HudPositionScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.client.hud_position"));
        this.parent = parent;
        ClientConfiguration config = ClientConfiguration.getInstance();
        this.offsetX = config.foodBarOffsetX;
        this.offsetY = config.foodBarOffsetY;
        this.previewSlots = buildPreviewSlots();
        this.boxWidth = Math.max(9, FoodBarOverlay.countIconSlots(previewSlots) * 8 + 1);
    }

    private static List<ConsumableFoodInstance> buildPreviewSlots() {
        Player player = Minecraft.getInstance().player;

        if (player instanceof DietHolder holder) {
            List<ConsumableFoodInstance> real = holder.getDiet().getSlots();
            if (!real.isEmpty()) return real;
        }
        return buildRandomPreviewSlots();
    }

    private static List<ConsumableFoodInstance> buildRandomPreviewSlots() {
        Random random = new Random();
        List<Item> pool = new ArrayList<>(List.of(PREVIEW_POOL));
        Collections.shuffle(pool, random);
        int count = 2 + random.nextInt(3);
        List<ConsumableFoodInstance> slots = new ArrayList<>();

        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            Item item = pool.get(i);
            FoodProperties properties = item.components().get(DataComponents.FOOD);
            ConsumableFoodInstance instance = ConsumableFoodInstance.create(item, properties);
            instance.tick(random.nextInt(Math.max(1, instance.duration())));
            slots.add(instance);
        }
        return slots;
    }

    @Override
    protected void init() {
        clampOffsets();
    }

    private int hudX() {
        Window window = Minecraft.getInstance().getWindow();
        return window.getGuiScaledWidth() / 2 + 10 + offsetX;
    }

    private int hudY() {
        Window window = Minecraft.getInstance().getWindow();
        return window.getGuiScaledHeight() - 39 + offsetY;
    }

    private void clampOffsets() {
        Window window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        int minX = 1 - (screenWidth / 2 + 10);
        int maxX = screenWidth - boxWidth - 1 - (screenWidth / 2 + 10);
        int minY = 1 - (screenHeight - 39);
        int maxY = screenHeight - boxHeight - 1 - (screenHeight - 39);

        offsetX = Math.clamp(offsetX, minX, maxX);
        offsetY = Math.clamp(offsetY, minY, maxY);
    }

    private boolean isHoveringHud(double mouseX, double mouseY) {
        int x = hudX();
        int y = hudY();
        return mouseX >= x - 1 && mouseX <= x + boxWidth + 1 && mouseY >= y - 1 && mouseY <= y + boxHeight + 1;
    }

    
    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getInstance().level == null) {
            graphics.fill(0, 0, width, height, 0xFF101010);
        }

        int x = hudX();
        int y = hudY();
        FoodBarOverlay.renderPreview(graphics, new Point(x, y), previewSlots);

        int centerY = height / 2;
        graphics.drawCenteredString(font, title, width / 2, centerY - 10, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.client.hud_position.hint").withStyle(ChatFormatting.GRAY), width / 2, centerY + 10, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHoveringHud(mouseX, mouseY)) {
            dragging = true;
            dragStartMouseX = mouseX;
            dragStartMouseY = mouseY;
            dragStartOffsetX = offsetX;
            dragStartOffsetY = offsetY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            offsetX = dragStartOffsetX + (int) Math.round(mouseX - dragStartMouseX);
            offsetY = dragStartOffsetY + (int) Math.round(mouseY - dragStartMouseY);
            clampOffsets();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        ClientConfiguration config = ClientConfiguration.getInstance();
        config.foodBarOffsetX = offsetX;
        config.foodBarOffsetY = offsetY;
        AutoConfig.getConfigHolder(ClientConfiguration.class).save();
        minecraft.setScreen(parent);
    }
}
