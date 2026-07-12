package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class FoodItemBonusScreen extends AbstractFoodConfigScreen {

    private static final int LIST_TOP = 80;
    private static final int HEADER_LABEL_Y = 64;

    private static final List<SuggestOption> OPERATIONS = List.of(
            new SuggestOption(AttributeModifier.Operation.ADD_VALUE.getSerializedName(), AttributeModifier.Operation.ADD_VALUE.getSerializedName(), AttributeModifier.Operation.ADD_VALUE.getSerializedName()),
            new SuggestOption(AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName()),
            new SuggestOption(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName())
    );

    private final Screen parent;
    private final FoodItemEntry entry;
    private final Runnable saveAction;
    private final List<BonusRow> rows = new ArrayList<>();
    private final @Nullable Item icon;
    private List<SuggestOption> attributeOptions = List.of();

    private EditBox newAttributeField;
    private EditBox newOperationField;
    private EditBox newAmountField;
    private EditBox newDurationField;

    public FoodItemBonusScreen(Screen parent, FoodItemEntry entry, Runnable saveAction) {
        super(Component.translatable("config.renourisheddelight.food_items.bonus_title"));
        this.parent = parent;
        this.entry = entry;
        this.saveAction = saveAction;
        this.icon = resolveItem(entry.item);
    }

    private static @Nullable Item resolveItem(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            return item != Items.AIR ? item : null;
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    protected void init() {
        attributeOptions = buildAttributeOptions();
        int centerX = width / 2;
        int newRowY = height - 56;

        newAttributeField = new EditBox(font, centerX - 240, newRowY, 180, 20, Component.translatable("config.renourisheddelight.food_items.attribute"));
        newAttributeField.setMaxLength(256);
        newAttributeField.setHint(Component.translatable("config.renourisheddelight.food_items.attribute").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newAttributeField);
        suggestFields.add(new SuggestField(newAttributeField, attributeOptions, true));

        newOperationField = new EditBox(font, centerX - 54, newRowY, 140, 20, Component.translatable("config.renourisheddelight.food_items.modifier"));
        newOperationField.setMaxLength(64);
        newOperationField.setHint(Component.translatable("config.renourisheddelight.food_items.modifier").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newOperationField);
        suggestFields.add(new SuggestField(newOperationField, OPERATIONS, true));

        newAmountField = new EditBox(font, centerX + 92, newRowY, 60, 20, Component.translatable("config.renourisheddelight.food_items.amount"));
        newAmountField.setMaxLength(32);
        newAmountField.setHint(Component.translatable("config.renourisheddelight.food_items.amount").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newAmountField);

        newDurationField = new EditBox(font, centerX + 158, newRowY, 60, 20, Component.translatable("config.renourisheddelight.food_items.duration"));
        newDurationField.setMaxLength(32);
        newDurationField.setHint(Component.translatable("config.renourisheddelight.food_items.duration").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newDurationField);

        addRenderableWidget(Button.builder(Component.literal("+"), button -> addBonus())
                .bounds(centerX + 224, newRowY, 20, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(centerX - 100, height - 28, 200, 20)
                .build());
        rebuildContent();
    }

    @Override
    protected void rebuildContent() {
        applyRows();

        for (BonusRow row : rows) {
            removeWidget(row.attribute());
            removeWidget(row.operation());
            removeWidget(row.amount());
            removeWidget(row.duration());
            removeWidget(row.remove());
        }
        rows.clear();
        suggestFields.removeIf(field -> field.box != newAttributeField && field.box != newOperationField);

        int centerX = width / 2;
        int listBottom = height - 68;
        int rowGap = ROW_HEIGHT - 20;
        int visibleRows = Math.max(1, (listBottom - LIST_TOP + rowGap) / ROW_HEIGHT);
        scrollMaxOffset = Math.max(0, entry.attributes.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, entry.attributes.size());

        scrollTrackX = centerX + 250;
        scrollTrackTop = LIST_TOP;
        scrollTrackBottom = listBottom;

        for (int i = 0; i < visibleRows && i + scrollOffset < entry.attributes.size(); i++) {
            AttributeBonus bonus = entry.attributes.get(i + scrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;

            EditBox attributeField = new EditBox(font, centerX - 240, y, 180, 20, Component.translatable("config.renourisheddelight.food_items.attribute"));
            attributeField.setMaxLength(256);
            attributeField.setValue(bonus.attribute);
            attributeField.setHint(Component.translatable("config.renourisheddelight.food_items.attribute").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(attributeField);
            suggestFields.add(new SuggestField(attributeField, attributeOptions));

            EditBox operationField = new EditBox(font, centerX - 54, y, 140, 20, Component.translatable("config.renourisheddelight.food_items.modifier"));
            operationField.setMaxLength(64);
            operationField.setValue(bonus.operation);
            operationField.setHint(Component.translatable("config.renourisheddelight.food_items.modifier").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(operationField);
            suggestFields.add(new SuggestField(operationField, OPERATIONS));

            EditBox amountField = new EditBox(font, centerX + 92, y, 60, 20, Component.translatable("config.renourisheddelight.food_items.amount"));
            amountField.setMaxLength(32);
            amountField.setValue(String.valueOf(bonus.amount));
            amountField.setHint(Component.translatable("config.renourisheddelight.food_items.amount").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(amountField);

            EditBox durationField = new EditBox(font, centerX + 158, y, 60, 20, Component.translatable("config.renourisheddelight.food_items.duration"));
            durationField.setMaxLength(32);
            durationField.setValue(String.valueOf(bonus.duration));
            durationField.setHint(Component.translatable("config.renourisheddelight.food_items.duration").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(durationField);

            Button removeButton = Button.builder(Component.literal("x"), button -> removeBonus(bonus))
                    .bounds(centerX + 224, y, 20, 20)
                    .build();
            addRenderableWidget(removeButton);

            rows.add(new BonusRow(bonus, attributeField, operationField, amountField, durationField, removeButton));
        }
    }

    private List<SuggestOption> buildAttributeOptions() {
        List<SuggestOption> options = new ArrayList<>();
        BuiltInRegistries.ATTRIBUTE.forEach(attribute -> {
            String id = BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString();
            String name = Component.translatable(attribute.getDescriptionId()).getString();
            String searchText = (id + " " + name).toLowerCase(Locale.ROOT);
            options.add(new SuggestOption(id, name, searchText));
        });
        options.sort(Comparator.comparing(SuggestOption::value, String.CASE_INSENSITIVE_ORDER));
        return options;
    }

    private void addBonus() {
        AttributeBonus bonus = new AttributeBonus(
                newAttributeField.getValue().trim(),
                newOperationField.getValue().trim(),
                parseDouble(newAmountField.getValue(), 0.0),
                parseInt(newDurationField.getValue(), 0));
        entry.attributes.add(bonus);
        scrollOffset = Integer.MAX_VALUE;

        newAttributeField.setValue("");
        newOperationField.setValue("");
        newAmountField.setValue("");
        newDurationField.setValue("");

        rebuildContent();
    }

    private void removeBonus(AttributeBonus bonus) {
        entry.attributes.remove(bonus);
        rebuildContent();
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

    @Override
    protected void onDone() {
        applyRows();
        saveAction.run();
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int subtitleY = 25;
        int subtitleTextY = subtitleY + 4;

        Component itemText = Component.literal(entry.item).withStyle(ChatFormatting.GRAY);
        if (icon != null) {
            String name = icon.getDescription().getString();
            itemText = Component.literal(name).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(" (" + entry.item + ")").withStyle(ChatFormatting.GRAY));
            graphics.renderItem(new ItemStack(icon), centerX - 240, subtitleY);
            graphics.drawString(font, itemText, centerX - 220, subtitleTextY, 0xFFFFFFFF);
        } else {
            graphics.drawString(font, itemText, centerX - 240, subtitleTextY, 0xFFFFFFFF);
        }

        applyRows();
        int maxDuration = entry.attributes.stream().mapToInt(bonus -> bonus.duration).max().orElse(0);
        Component durationText = Component.translatable("config.renourisheddelight.food_items.duration").copy().withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(StringUtil.formatTickDuration(maxDuration, 20)).withStyle(ChatFormatting.WHITE));
        graphics.drawString(font, durationText, centerX + 244 - font.width(durationText), subtitleTextY, 0xFFFFFF);

        graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.attribute"), centerX - 240, HEADER_LABEL_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.modifier"), centerX - 54, HEADER_LABEL_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.amount"), centerX + 92, HEADER_LABEL_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.duration"), centerX + 158, HEADER_LABEL_Y, 0xFFFFFF);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (entry.attributes.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.food_items.no_bonuses"), width / 2, height / 2, 0xAAAAAA);
        }
    }

    private record BonusRow(AttributeBonus bonus, EditBox attribute, EditBox operation, EditBox amount, EditBox duration, Button remove) {
        // do nothing
    }
}
