package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.Configuration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class FoodItemBonusScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int SUGGESTION_ROW_HEIGHT = 12;
    private static final int MAX_SUGGESTIONS = 15;

    private static final List<SuggestOption> OPERATIONS = List.of(
            new SuggestOption(AttributeModifier.Operation.ADD_VALUE.getSerializedName(), AttributeModifier.Operation.ADD_VALUE.getSerializedName(), AttributeModifier.Operation.ADD_VALUE.getSerializedName()),
            new SuggestOption(AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName()),
            new SuggestOption(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName())
    );

    private final Screen parent;
    private final Configuration.FoodItemEntry entry;
    private final List<BonusRow> rows = new ArrayList<>();
    private final List<SuggestField> suggestFields = new ArrayList<>();
    private final @Nullable Item icon;
    private List<SuggestOption> attributeOptions = List.of();

    private EditBox newAttributeField;
    private EditBox newOperationField;
    private EditBox newAmountField;
    private EditBox newDurationField;

    public FoodItemBonusScreen(Screen parent, Configuration.FoodItemEntry entry) {
        super(Component.literal(entry.item));
        this.parent = parent;
        this.entry = entry;
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

        newAttributeField = new EditBox(font, centerX - 240, newRowY, 180, 20, Component.literal("attribute"));
        newAttributeField.setMaxLength(256);
        newAttributeField.setHint(Component.literal("Attribute").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newAttributeField);
        suggestFields.add(new SuggestField(newAttributeField, attributeOptions, true));

        newOperationField = new EditBox(font, centerX - 54, newRowY, 140, 20, Component.literal("operation"));
        newOperationField.setMaxLength(64);
        newOperationField.setHint(Component.literal("Modifier").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newOperationField);
        suggestFields.add(new SuggestField(newOperationField, OPERATIONS, true));

        newAmountField = new EditBox(font, centerX + 92, newRowY, 60, 20, Component.literal("amount"));
        newAmountField.setMaxLength(32);
        newAmountField.setValue("0.0");
        addRenderableWidget(newAmountField);

        newDurationField = new EditBox(font, centerX + 158, newRowY, 60, 20, Component.literal("duration"));
        newDurationField.setMaxLength(32);
        newDurationField.setValue("0");
        addRenderableWidget(newDurationField);

        addRenderableWidget(Button.builder(Component.literal("+"), button -> addBonus())
                .bounds(centerX + 224, newRowY, 20, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(centerX - 100, height - 28, 200, 20)
                .build());
        rebuildRows();
    }

    private void rebuildRows() {
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
        int top = 52;

        for (int i = 0; i < entry.attributes.size(); i++) {
            Configuration.AttributeBonus bonus = entry.attributes.get(i);
            int y = top + i * ROW_HEIGHT;

            EditBox attributeField = new EditBox(font, centerX - 240, y, 180, 20, Component.literal("attribute"));
            attributeField.setMaxLength(256);
            attributeField.setValue(bonus.attribute);
            attributeField.setHint(Component.literal("Attribute").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(attributeField);
            suggestFields.add(new SuggestField(attributeField, attributeOptions));

            EditBox operationField = new EditBox(font, centerX - 54, y, 140, 20, Component.literal("operation"));
            operationField.setMaxLength(64);
            operationField.setValue(bonus.operation);
            operationField.setHint(Component.literal("Modifier").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(operationField);
            suggestFields.add(new SuggestField(operationField, OPERATIONS));

            EditBox amountField = new EditBox(font, centerX + 92, y, 60, 20, Component.literal("amount"));
            amountField.setMaxLength(32);
            amountField.setValue(String.valueOf(bonus.amount));
            addRenderableWidget(amountField);

            EditBox durationField = new EditBox(font, centerX + 158, y, 60, 20, Component.literal("duration"));
            durationField.setMaxLength(32);
            durationField.setValue(String.valueOf(bonus.duration));
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
            String label = id + " (" + name + ")";
            String searchText = (id + " " + name).toLowerCase(Locale.ROOT);
            options.add(new SuggestOption(id, label, searchText));
        });
        options.sort(Comparator.comparing(SuggestOption::value, String.CASE_INSENSITIVE_ORDER));
        return options;
    }

    private void addBonus() {
        applyRows();
        Configuration.AttributeBonus bonus = new Configuration.AttributeBonus();
        bonus.attribute = newAttributeField.getValue().trim();
        bonus.operation = newOperationField.getValue().trim();
        bonus.amount = parseDouble(newAmountField.getValue(), 0.0);
        bonus.duration = parseInt(newDurationField.getValue(), 0);
        entry.attributes.add(bonus);

        newAttributeField.setValue("");
        newOperationField.setValue("");
        newAmountField.setValue("0.0");
        newDurationField.setValue("0");

        rebuildRows();
    }

    private void removeBonus(Configuration.AttributeBonus bonus) {
        applyRows();
        entry.attributes.remove(bonus);
        rebuildRows();
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (SuggestField field : suggestFields) {
            if (field.box.isFocused() && !field.matches.isEmpty()) {
                int index = field.indexAt(mouseX, mouseY);
                if (index >= 0) {
                    field.box.setValue(field.matches.get(index).value());
                    field.matches = List.of();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int titleWidth = font.width(title);
        if (icon != null) {
            graphics.renderItem(new ItemStack(icon), centerX - titleWidth / 2 - 20, 8);
        }
        graphics.drawCenteredString(font, title, centerX, 12, 0xFFFFFF);

        int headerY = 40;
        graphics.drawString(font, "Attribute", centerX - 240, headerY, 0xFFFFFF);
        graphics.drawString(font, "Modifier", centerX - 54, headerY, 0xFFFFFF);
        graphics.drawString(font, "Amount", centerX + 92, headerY, 0xFFFFFF);
        graphics.drawString(font, "Duration", centerX + 158, headerY, 0xFFFFFF);

        if (entry.attributes.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.food_items.no_bonuses"), width / 2, height / 2, 0xAAAAAA);
        }

        for (SuggestField field : suggestFields) {
            if (field.box.isFocused() && !field.matches.isEmpty()) {
                field.render(graphics, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        onDone();
    }

    private record BonusRow(Configuration.AttributeBonus bonus, EditBox attribute, EditBox operation, EditBox amount, EditBox duration, Button remove) {
    }

    private record SuggestOption(String value, String label, String searchText) {
    }

    private final class SuggestField {
        private final EditBox box;
        private final List<SuggestOption> pool;
        private final boolean dropUp;
        private List<SuggestOption> matches = List.of();

        private SuggestField(EditBox box, List<SuggestOption> pool) {
            this(box, pool, false);
        }

        private SuggestField(EditBox box, List<SuggestOption> pool, boolean dropUp) {
            this.box = box;
            this.pool = pool;
            this.dropUp = dropUp;
            box.setResponder(value -> updateMatches());
            updateMatches();
        }

        private void updateMatches() {
            String query = box.getValue().trim().toLowerCase(Locale.ROOT);
            matches = pool.stream()
                    .filter(option -> query.isEmpty() || option.searchText().contains(query))
                    .sorted(Comparator
                            .<SuggestOption>comparingInt(option -> option.value().toLowerCase(Locale.ROOT).startsWith(query) ? 0 : 1)
                            .thenComparing(option -> option.value(), String.CASE_INSENSITIVE_ORDER))
                    .limit(MAX_SUGGESTIONS)
                    .toList();
        }

        private int listTop() {
            return dropUp
                    ? box.getY() - matches.size() * SUGGESTION_ROW_HEIGHT
                    : box.getY() + box.getHeight();
        }

        private int indexAt(double mouseX, double mouseY) {
            int x = box.getX();
            int y = listTop();
            int listWidth = box.getWidth();
            if (mouseX < x || mouseX > x + listWidth) return -1;
            if (mouseY < y || mouseY > y + matches.size() * SUGGESTION_ROW_HEIGHT) return -1;
            return (int) ((mouseY - y) / SUGGESTION_ROW_HEIGHT);
        }

        private void render(GuiGraphics graphics, int mouseX, int mouseY) {
            int x = box.getX();
            int y = listTop();
            int listWidth = box.getWidth();
            graphics.fill(x, y, x + listWidth, y + matches.size() * SUGGESTION_ROW_HEIGHT, 0xE0000000);
            for (int i = 0; i < matches.size(); i++) {
                int rowY = y + i * SUGGESTION_ROW_HEIGHT;
                boolean hovered = mouseX >= x && mouseX <= x + listWidth && mouseY >= rowY && mouseY <= rowY + SUGGESTION_ROW_HEIGHT;
                int color = hovered ? 0xFFFF00 : 0xAAAAAA;
                graphics.drawString(font, matches.get(i).label(), x + 2, rowY + 2, color, false);
            }
        }
    }
}
