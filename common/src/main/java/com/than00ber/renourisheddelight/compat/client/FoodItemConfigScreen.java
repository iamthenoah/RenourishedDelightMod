package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.Configuration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FoodItemConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;

    private final @Nullable Screen parent;
    private EditBox newItemField;
    private int scrollOffset = 0;

    public FoodItemConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.food_items"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        int centerX = width / 2;

        newItemField = new EditBox(font, centerX - 150, 30, 220, 20, Component.translatable("config.renourisheddelight.food_items.new_item"));
        newItemField.setMaxLength(256);
        newItemField.setHint(Component.literal("minecraft:bread"));
        addRenderableWidget(newItemField);

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.food_items.add"), button -> addItem())
                .bounds(centerX + 75, 30, 75, 20)
                .build());

        List<Configuration.FoodItemEntry> entries = Configuration.Common.getInstance().foodItemConfigurations;
        int listTop = 58;
        int listBottom = height - 36;
        int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        int maxOffset = Math.max(0, entries.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxOffset);

        for (int i = 0; i < visibleRows && i + scrollOffset < entries.size(); i++) {
            Configuration.FoodItemEntry entry = entries.get(i + scrollOffset);
            int y = listTop + i * ROW_HEIGHT;

            addRenderableWidget(Button.builder(Component.literal(entry.item), button -> openBonuses(entry))
                    .bounds(centerX - 150, y, 260, 20)
                    .build());

            addRenderableWidget(Button.builder(Component.literal("X"), button -> removeItem(entry))
                    .bounds(centerX + 115, y, 35, 20)
                    .build());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(centerX - 100, height - 28, 200, 20)
                .build());
    }

    private void addItem() {
        String value = newItemField.getValue().trim();
        if (value.isEmpty()) return;
        ResourceLocation id;

        try {
            id = ResourceLocation.parse(value);
        } catch (Exception exception) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) return;
        String key = id.toString();
        List<Configuration.FoodItemEntry> entries = Configuration.Common.getInstance().foodItemConfigurations;

        for (Configuration.FoodItemEntry entry : entries) {
            if (entry.item.equals(key)) {
                newItemField.setValue("");
                openBonuses(entry);
                return;
            }
        }
        Configuration.FoodItemEntry entry = new Configuration.FoodItemEntry();
        entry.item = key;
        entries.add(entry);
        newItemField.setValue("");
        AutoConfig.getConfigHolder(Configuration.Common.class).save();
        rebuild();
    }

    private void removeItem(Configuration.FoodItemEntry entry) {
        Configuration.Common.getInstance().foodItemConfigurations.remove(entry);
        AutoConfig.getConfigHolder(Configuration.Common.class).save();
        rebuild();
    }

    private void openBonuses(Configuration.FoodItemEntry entry) {
        minecraft.setScreen(new FoodItemBonusScreen(this, entry));
    }

    private void onDone() {
        AutoConfig.getConfigHolder(Configuration.Common.class).save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= (int) Math.signum(scrollY);
        if (scrollOffset < 0) scrollOffset = 0;
        rebuild();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        onDone();
    }
}
