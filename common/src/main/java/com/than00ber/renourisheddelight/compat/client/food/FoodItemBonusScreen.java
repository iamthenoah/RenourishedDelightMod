package com.than00ber.renourisheddelight.compat.client.food;

import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class FoodItemBonusScreen extends AbstractFoodConfigScreen {

    private static final int ORANGE_TEXT_COLOR = 0xFFAA00;
    private static final ItemStack BARRIER_ICON = new ItemStack(Items.BARRIER);
    private static final List<SuggestOption> OPERATIONS = Stream.of(AttributeModifier.Operation.values())
            .map(AttributeModifier.Operation::getSerializedName)
            .map(name -> new SuggestOption(name, name, name))
            .toList();

    private final Item item;
    private final List<BonusRow> rows = new ArrayList<>();
    private List<SuggestOption> attributeOptions = List.of();

    private EditBox newAttributeField;
    private EditBox newOperationField;
    private EditBox newAmountField;
    private EditBox newDurationField;

    public FoodItemBonusScreen(FoodItemConfigScreen parent, Item item) {
        super(Component.translatable("config.renourisheddelight.food_items.bonus_title"), parent, parent.config);
        this.item = item;
    }

    private FoodItemEntry entry() {
        return config.claim(presets, item);
    }

    @Override
    protected void init() {
        attributeOptions = buildOptions(BuiltInRegistries.ATTRIBUTE.stream(), attribute -> BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString(), attribute -> Component.translatable(attribute.getDescriptionId()).getString());
        int centerX = width / 2;
        int newRowY = inputRowY();

        newAttributeField = addField(centerX - 240, newRowY, 180, "config.renourisheddelight.food_items.attribute", 256);
        suggestFields.add(new SuggestField(newAttributeField, attributeOptions, true));
        newOperationField = addField(centerX - 54, newRowY, 140, "config.renourisheddelight.food_items.modifier", 64);
        suggestFields.add(new SuggestField(newOperationField, OPERATIONS, true));
        newAmountField = addField(centerX + 92, newRowY, 60, "config.renourisheddelight.food_items.amount", 32);
        newDurationField = addField(centerX + 158, newRowY, 60, "config.renourisheddelight.food_items.duration", 32);
        addSmallButton("+", centerX + 224, newRowY, this::addBonus);

        addFooterButtons(this::resetBonuses);
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
        int visibleRows = layoutList(LIST_TOP, BUTTON_HEIGHT, entry().attributes.size(), centerX + 250);

        for (int i = 0; i < visibleRows && i + scrollOffset < entry().attributes.size(); i++) {
            AttributeBonus bonus = entry().attributes.get(i + scrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;

            EditBox attributeField = addField(centerX - 240, y, 180, "config.renourisheddelight.food_items.attribute", 256);
            attributeField.setValue(bonus.attribute != null ? bonus.attribute : "");
            suggestFields.add(new SuggestField(attributeField, attributeOptions));

            EditBox operationField = addField(centerX - 54, y, 140, "config.renourisheddelight.food_items.modifier", 64);
            operationField.setValue(bonus.operation != null ? bonus.operation : "");
            suggestFields.add(new SuggestField(operationField, OPERATIONS));

            EditBox amountField = addField(centerX + 92, y, 60, "config.renourisheddelight.food_items.amount", 32);
            amountField.setValue(String.valueOf(bonus.amount));

            EditBox durationField = addField(centerX + 158, y, 60, "config.renourisheddelight.food_items.duration", 32);
            durationField.setValue(String.valueOf(bonus.duration));

            Button removeButton = addSmallButton("x", centerX + 224, y, () -> removeBonus(bonus));
            rows.add(new BonusRow(bonus, attributeField, operationField, amountField, durationField, removeButton));
        }
    }

    private void addBonus() {
        entry().attributes.add(pendingBonus());
        scrollOffset = Integer.MAX_VALUE;
        newAttributeField.setValue("");
        newOperationField.setValue("");
        newAmountField.setValue("");
        newDurationField.setValue("");
        rebuildContent();
    }

    private AttributeBonus pendingBonus() {
        return new AttributeBonus(newAttributeField.getValue().trim(), newOperationField.getValue().trim(), parseDouble(newAmountField.getValue(), 0.0), parseInt(newDurationField.getValue(), 0));
    }

    private boolean hasPendingBonus() {
        return (!newAttributeField.getValue().isBlank() || !newOperationField.getValue().isBlank() || !newAmountField.getValue().isBlank() || !newDurationField.getValue().isBlank());
    }

    private void removeBonus(AttributeBonus bonus) {
        entry().attributes.remove(bonus);
        rebuildContent();
    }

    private void resetBonuses() {
        entry().attributes.clear();
        entry().attributes.addAll(FoodItemEntry.baseline(presets, item));
        scrollOffset = 0;
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

    private boolean isValidAttribute(String value) {
        return ConsumableFoodInstance.resolveAttribute(value.trim()) != null;
    }

    private boolean isValidOperation(String value) {
        String trimmed = value.trim();
        return OPERATIONS.stream().anyMatch(option -> option.value().equalsIgnoreCase(trimmed));
    }

    private boolean isValidAmount(String value) {
        return parseDouble(value, 0.0) != 0.0;
    }

    private boolean isValidDuration(String value) {
        return parseInt(value, 0) != 0;
    }

    private boolean isValidRow(BonusRow row) {
        return isValidAttribute(row.attribute().getValue()) && isValidOperation(row.operation().getValue()) && isValidAmount(row.amount().getValue()) && isValidDuration(row.duration().getValue());
    }

    private @Nullable Component attributeTooltip(String value) {
        return isValidAttribute(value) ? null : Component.translatable("config.renourisheddelight.food_items.attribute_invalid");
    }

    private @Nullable Component operationTooltip(String value) {
        return isValidOperation(value) ? null : Component.translatable("config.renourisheddelight.food_items.modifier_invalid");
    }

    private @Nullable Component durationTooltip(String value) {
        try {
            return Component.literal(StringUtil.formatTickDuration(Integer.parseInt(value.trim()), 20));
        } catch (Exception exception) {
            return null;
        }
    }

    private @Nullable Component amountTooltip(String amountValue, String operationValue) {
        double amount;
        try {
            amount = Double.parseDouble(amountValue.trim());
        } catch (Exception exception) {
            return null;
        }
        String operation = operationValue.trim().toLowerCase(Locale.ROOT);
        boolean percent = operation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName()) || operation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName());
        double display = percent ? amount * 100.0 : amount;
        String sign = display > 0 ? "+" : "";
        return Component.literal(sign + formatNumber(display) + (percent ? "%" : ""));
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }

    private void applyValidationColors() {
        for (BonusRow row : rows) {
            row.attribute().setTextColor(isValidAttribute(row.attribute().getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
            row.operation().setTextColor(isValidOperation(row.operation().getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
            row.amount().setTextColor(isValidAmount(row.amount().getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
            row.duration().setTextColor(isValidDuration(row.duration().getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        }
        newAttributeField.setTextColor(newAttributeField.getValue().isEmpty() || isValidAttribute(newAttributeField.getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
        newOperationField.setTextColor(newOperationField.getValue().isEmpty() || isValidOperation(newOperationField.getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
        newAmountField.setTextColor(newAmountField.getValue().isEmpty() || isValidAmount(newAmountField.getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        newDurationField.setTextColor(newDurationField.getValue().isEmpty() || isValidDuration(newDurationField.getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private boolean isHoveringBarrier(EditBox attributeField, int mouseX, int mouseY) {
        int x = attributeField.getX() - 20;
        int y = attributeField.getY() + 2;
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    private void showFieldTooltip(EditBox box, @Nullable Component text, int mouseX, int mouseY) {
        if (text != null && isHovering(box, mouseX, mouseY)) {
            setTooltipForNextRenderPass(text);
        }
    }

    private void renderTooltips(int mouseX, int mouseY) {
        for (BonusRow row : rows) {
            if (!isValidRow(row) && isHoveringBarrier(row.attribute(), mouseX, mouseY)) {
                setTooltipForNextRenderPass(Component.translatable("config.renourisheddelight.food_items.bonus_invalid"));
            }
            showFieldTooltip(row.attribute(), attributeTooltip(row.attribute().getValue()), mouseX, mouseY);
            showFieldTooltip(row.operation(), operationTooltip(row.operation().getValue()), mouseX, mouseY);
            showFieldTooltip(row.amount(), amountTooltip(row.amount().getValue(), row.operation().getValue()), mouseX, mouseY);
            showFieldTooltip(row.duration(), durationTooltip(row.duration().getValue()), mouseX, mouseY);
        }
        showFieldTooltip(newAttributeField, newAttributeField.getValue().isEmpty() ? null : attributeTooltip(newAttributeField.getValue()), mouseX, mouseY);
        showFieldTooltip(newOperationField, newOperationField.getValue().isEmpty() ? null : operationTooltip(newOperationField.getValue()), mouseX, mouseY);
        showFieldTooltip(newAmountField, newAmountField.getValue().isEmpty() ? null : amountTooltip(newAmountField.getValue(), newOperationField.getValue()), mouseX, mouseY);
        showFieldTooltip(newDurationField, newDurationField.getValue().isEmpty() ? null : durationTooltip(newDurationField.getValue()), mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        applyValidationColors();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltips(mouseX, mouseY);
    }

    // edits the parent screen's working copy, which the parent saves when it closes
    @Override
    protected void save() {
        applyRows();
        if (hasPendingBonus()) entry().attributes.add(pendingBonus());
        config.prune(presets, item);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int subtitleY = 25;
        int subtitleTextY = subtitleY + 4;

        Component itemText = Component.literal(item.getDescription().getString())
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" (" + BuiltInRegistries.ITEM.getKey(item) + ")").withStyle(ChatFormatting.GRAY));
        graphics.renderItem(new ItemStack(item), centerX - 240, subtitleY);
        graphics.drawString(font, itemText, centerX - 220, subtitleTextY, 0xFFFFFFFF);

        applyRows();
        int maxDuration = entry().attributes.stream().mapToInt(bonus -> bonus.duration).max().orElse(0);
        Component durationText = Component.translatable("config.renourisheddelight.food_items.duration")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(StringUtil.formatTickDuration(maxDuration, 20)).withStyle(ChatFormatting.WHITE));
        graphics.drawString(font, durationText, centerX + 244 - font.width(durationText), subtitleTextY, 0xFFFFFF);

        drawHeaderLabel(graphics, "config.renourisheddelight.food_items.attribute", centerX - 240);
        drawHeaderLabel(graphics, "config.renourisheddelight.food_items.modifier", centerX - 54);
        drawHeaderLabel(graphics, "config.renourisheddelight.food_items.amount", centerX + 92);
        drawHeaderLabel(graphics, "config.renourisheddelight.food_items.duration", centerX + 158);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (entry().attributes.isEmpty()) {
            drawCenteredMessage(graphics, "config.renourisheddelight.food_items.no_bonuses");
        }
        for (BonusRow row : rows) {
            if (!isValidRow(row)) {
                graphics.renderItem(BARRIER_ICON, row.attribute().getX() - 20, row.attribute().getY() + 2);
            }
        }
    }

    private record BonusRow(AttributeBonus bonus, EditBox attribute, EditBox operation, EditBox amount, EditBox duration, Button remove) {
    }
}
