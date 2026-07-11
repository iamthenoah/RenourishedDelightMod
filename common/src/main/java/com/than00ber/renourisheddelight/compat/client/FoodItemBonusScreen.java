package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.Configuration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class FoodItemBonusScreen extends Screen {

    private static final int ROW_HEIGHT = 24;

    private final Screen parent;
    private final Configuration.FoodItemEntry entry;
    private final List<BonusRow> rows = new ArrayList<>();

    public FoodItemBonusScreen(Screen parent, Configuration.FoodItemEntry entry) {
        super(Component.literal(entry.item));
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        rows.clear();
        int centerX = width / 2;
        int top = 36;

        for (int i = 0; i < entry.attributes.size(); i++) {
            Configuration.AttributeBonus bonus = entry.attributes.get(i);
            int y = top + i * ROW_HEIGHT;

            EditBox attributeField = new EditBox(font, centerX - 220, y, 140, 20, Component.literal("attribute"));
            attributeField.setMaxLength(256);
            attributeField.setValue(bonus.attribute);
            addRenderableWidget(attributeField);

            EditBox operationField = new EditBox(font, centerX - 75, y, 110, 20, Component.literal("operation"));
            operationField.setMaxLength(64);
            operationField.setValue(bonus.operation);
            addRenderableWidget(operationField);

            EditBox amountField = new EditBox(font, centerX + 40, y, 60, 20, Component.literal("amount"));
            amountField.setMaxLength(32);
            amountField.setValue(String.valueOf(bonus.amount));
            addRenderableWidget(amountField);

            EditBox durationField = new EditBox(font, centerX + 105, y, 60, 20, Component.literal("duration"));
            durationField.setMaxLength(32);
            durationField.setValue(String.valueOf(bonus.duration));
            addRenderableWidget(durationField);

            addRenderableWidget(Button.builder(Component.literal("X"), button -> removeBonus(bonus))
                    .bounds(centerX + 170, y, 20, 20)
                    .build());
            rows.add(new BonusRow(bonus, attributeField, operationField, amountField, durationField));
        }

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.food_items.add_bonus"), button -> addBonus())
                .bounds(centerX - 100, top + entry.attributes.size() * ROW_HEIGHT + 6, 200, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(centerX - 100, height - 28, 200, 20)
                .build());
    }

    private void addBonus() {
        applyRows();
        entry.attributes.add(new Configuration.AttributeBonus());
        rebuild();
    }

    private void removeBonus(Configuration.AttributeBonus bonus) {
        applyRows();
        entry.attributes.remove(bonus);
        rebuild();
    }

    private void applyRows() {
        for (BonusRow row : rows) {
            row.bonus().attribute = row.attribute().getValue().trim();
            row.bonus().operation = row.operation().getValue().trim();
            row.bonus().amount = parseDouble(row.amount().getValue(), row.bonus().amount);
            row.bonus().duration = parseInt(row.duration().getValue(), row.bonus().duration);
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private void onDone() {
        applyRows();
        AutoConfig.getConfigHolder(Configuration.Common.class).save();
        minecraft.setScreen(parent);
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

    private record BonusRow(Configuration.AttributeBonus bonus, EditBox attribute, EditBox operation, EditBox amount, EditBox duration) {
        // do nothing
    }
}
