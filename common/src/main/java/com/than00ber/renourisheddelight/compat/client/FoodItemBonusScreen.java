package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.EffectBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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
import java.util.function.Consumer;

public final class FoodItemBonusScreen extends AbstractFoodConfigScreen {

    private static final int LIST_TOP = 92;
    private static final int HEADER_LABEL_Y = 76;
    private static final int SUBTITLE_Y = 30;
    private static final int TAB_ATTRIBUTES = 0;
    private static final int TAB_EFFECTS = 1;
    private static final int NORMAL_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xFF5555;
    private static final int ORANGE_TEXT_COLOR = 0xFFAA00;
    private static final ItemStack BARRIER_ICON = new ItemStack(Items.BARRIER);

    private static final List<SuggestOption> OPERATIONS = List.of(
            new SuggestOption(AttributeModifier.Operation.ADD_VALUE.getSerializedName(), AttributeModifier.Operation.ADD_VALUE.getSerializedName(), AttributeModifier.Operation.ADD_VALUE.getSerializedName()),
            new SuggestOption(AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName()),
            new SuggestOption(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName())
    );

    private final Screen parent;
    private final FoodItemEntry entry;
    private final Runnable saveAction;
    private final List<BonusRow> rows = new ArrayList<>();
    private final List<EffectRow> effectRows = new ArrayList<>();
    private final TabManager tabManager = new TabManager(this::addViewWidget, this::removeWidget);
    private final @Nullable Item icon;
    private List<SuggestOption> attributeOptions = List.of();
    private List<SuggestOption> effectOptions = List.of();

    private EditBox newAttributeField;
    private EditBox newOperationField;
    private EditBox newAmountField;
    private EditBox newDurationField;
    private Button addBonusButton;
    private EditBox newEffectField;
    private EditBox newLevelField;
    private EditBox newEffectDurationField;
    private Button addEffectButton;

    private int activeTab = TAB_ATTRIBUTES;
    private boolean initialised;

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
    protected boolean showTitle() {
        return false;
    }

    @Override
    protected void init() {
        initialised = false;
        attributeOptions = buildAttributeOptions();
        effectOptions = buildEffectOptions();
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

        addBonusButton = addRenderableWidget(Button.builder(Component.literal("+"), button -> addBonus())
                .bounds(centerX + 224, newRowY, 20, 20)
                .build());

        newEffectField = new EditBox(font, centerX - 240, newRowY, 326, 20, Component.translatable("config.renourisheddelight.food_items.effect"));
        newEffectField.setMaxLength(256);
        newEffectField.setHint(Component.translatable("config.renourisheddelight.food_items.effect").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newEffectField);
        suggestFields.add(new SuggestField(newEffectField, effectOptions, true));

        newLevelField = new EditBox(font, centerX + 92, newRowY, 60, 20, Component.translatable("config.renourisheddelight.food_items.level"));
        newLevelField.setMaxLength(32);
        newLevelField.setHint(Component.translatable("config.renourisheddelight.food_items.level").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newLevelField);

        newEffectDurationField = new EditBox(font, centerX + 158, newRowY, 60, 20, Component.translatable("config.renourisheddelight.food_items.duration"));
        newEffectDurationField.setMaxLength(32);
        newEffectDurationField.setHint(Component.translatable("config.renourisheddelight.food_items.duration").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newEffectDurationField);

        addEffectButton = addRenderableWidget(Button.builder(Component.literal("+"), button -> addEffect())
                .bounds(centerX + 224, newRowY, 20, 20)
                .build());

        int buttonsY = height - 28;
        int buttonsWidth = 200;
        int buttonsLeft = centerX - buttonsWidth / 2;
        int gap = 5;
        int halfWidth = (buttonsWidth - gap) / 2;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(buttonsLeft, buttonsY, halfWidth, 20)
                .build());
        addRenderableWidget(createResetButton(buttonsLeft + halfWidth + gap, buttonsY, buttonsWidth - halfWidth - gap, 20, this::resetBonuses));

        TabNavigationBar tabs = TabNavigationBar.builder(tabManager, width)
                .addTabs(new AttributeTab(), new EffectTab())
                .build();
        addViewWidget(tabs);
        tabs.arrangeElements();
        tabManager.setTabArea(new ScreenRectangle(0, tabs.getRectangle().bottom(), width, height - tabs.getRectangle().bottom()));
        tabs.selectTab(activeTab, false);

        initialised = true;
        rebuildContent();
    }

    private void selectTab(int tab) {
        if (activeTab != tab) scrollOffset = 0;
        activeTab = tab;
        if (initialised) rebuildContent();
    }

    private boolean onAttributeTab() {
        return activeTab == TAB_ATTRIBUTES;
    }

    @Override
    protected void rebuildContent() {
        applyRows();
        applyEffectRows();

        for (BonusRow row : rows) {
            removeWidget(row.attribute());
            removeWidget(row.operation());
            removeWidget(row.amount());
            removeWidget(row.duration());
            removeWidget(row.remove());
        }
        rows.clear();

        for (EffectRow row : effectRows) {
            removeWidget(row.effect());
            removeWidget(row.level());
            removeWidget(row.duration());
            removeWidget(row.remove());
        }
        effectRows.clear();
        suggestFields.removeIf(field -> field.box != newAttributeField && field.box != newOperationField && field.box != newEffectField);

        boolean attributes = onAttributeTab();
        setPendingVisible(newAttributeField, attributes);
        setPendingVisible(newOperationField, attributes);
        setPendingVisible(newAmountField, attributes);
        setPendingVisible(newDurationField, attributes);
        addBonusButton.visible = attributes;
        setPendingVisible(newEffectField, !attributes);
        setPendingVisible(newLevelField, !attributes);
        setPendingVisible(newEffectDurationField, !attributes);
        addEffectButton.visible = !attributes;

        int centerX = width / 2;
        int listBottom = height - 68;
        int rowGap = ROW_HEIGHT - 20;
        int visibleRows = Math.max(1, (listBottom - LIST_TOP + rowGap) / ROW_HEIGHT);
        int totalRows = attributes ? entry.attributes.size() : entry.effects.size();
        scrollMaxOffset = Math.max(0, totalRows - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, totalRows);

        scrollTrackX = centerX + 250;
        scrollTrackTop = LIST_TOP;
        scrollTrackBottom = listBottom;

        for (int i = 0; i < visibleRows && i + scrollOffset < totalRows; i++) {
            int y = LIST_TOP + i * ROW_HEIGHT;
            if (attributes) {
                buildBonusRow(entry.attributes.get(i + scrollOffset), centerX, y);
            } else {
                buildEffectRow(entry.effects.get(i + scrollOffset), centerX, y);
            }
        }
    }

    private void setPendingVisible(EditBox box, boolean visible) {
        if (!visible) box.setFocused(false);
        box.setVisible(visible);
    }

    private void buildBonusRow(AttributeBonus bonus, int centerX, int y) {
        EditBox attributeField = new EditBox(font, centerX - 240, y, 180, 20, Component.translatable("config.renourisheddelight.food_items.attribute"));
        attributeField.setMaxLength(256);
        attributeField.setValue(bonus.attribute != null ? bonus.attribute : "");
        attributeField.setHint(Component.translatable("config.renourisheddelight.food_items.attribute").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(attributeField);
        suggestFields.add(new SuggestField(attributeField, attributeOptions));

        EditBox operationField = new EditBox(font, centerX - 54, y, 140, 20, Component.translatable("config.renourisheddelight.food_items.modifier"));
        operationField.setMaxLength(64);
        operationField.setValue(bonus.operation != null ? bonus.operation : "");
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

    private void buildEffectRow(EffectBonus bonus, int centerX, int y) {
        EditBox effectField = new EditBox(font, centerX - 240, y, 326, 20, Component.translatable("config.renourisheddelight.food_items.effect"));
        effectField.setMaxLength(256);
        effectField.setValue(bonus.effect != null ? bonus.effect : "");
        effectField.setHint(Component.translatable("config.renourisheddelight.food_items.effect").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(effectField);
        suggestFields.add(new SuggestField(effectField, effectOptions));

        EditBox levelField = new EditBox(font, centerX + 92, y, 60, 20, Component.translatable("config.renourisheddelight.food_items.level"));
        levelField.setMaxLength(32);
        levelField.setValue(String.valueOf(bonus.level));
        levelField.setHint(Component.translatable("config.renourisheddelight.food_items.level").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(levelField);

        EditBox durationField = new EditBox(font, centerX + 158, y, 60, 20, Component.translatable("config.renourisheddelight.food_items.duration"));
        durationField.setMaxLength(32);
        durationField.setValue(String.valueOf(bonus.duration));
        durationField.setHint(Component.translatable("config.renourisheddelight.food_items.duration").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(durationField);

        Button removeButton = Button.builder(Component.literal("x"), button -> removeEffect(bonus))
                .bounds(centerX + 224, y, 20, 20)
                .build();
        addRenderableWidget(removeButton);

        effectRows.add(new EffectRow(bonus, effectField, levelField, durationField, removeButton));
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

    private List<SuggestOption> buildEffectOptions() {
        List<SuggestOption> options = new ArrayList<>();
        BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
            String id = BuiltInRegistries.MOB_EFFECT.getKey(effect).toString();
            String name = effect.getDisplayName().getString();
            options.add(new SuggestOption(id, name, (id + " " + name).toLowerCase(Locale.ROOT)));
        });
        options.sort(Comparator.comparing(SuggestOption::value, String.CASE_INSENSITIVE_ORDER));
        return options;
    }

    private void addBonus() {
        entry.attributes.add(pendingBonus());
        scrollOffset = Integer.MAX_VALUE;

        newAttributeField.setValue("");
        newOperationField.setValue("");
        newAmountField.setValue("");
        newDurationField.setValue("");

        rebuildContent();
    }

    private void addEffect() {
        entry.effects.add(pendingEffect());
        scrollOffset = Integer.MAX_VALUE;

        newEffectField.setValue("");
        newLevelField.setValue("");
        newEffectDurationField.setValue("");

        rebuildContent();
    }

    private AttributeBonus pendingBonus() {
        return new AttributeBonus(
                newAttributeField.getValue().trim(),
                newOperationField.getValue().trim(),
                parseDouble(newAmountField.getValue(), 0.0),
                parseInt(newDurationField.getValue(), 0));
    }

    private EffectBonus pendingEffect() {
        return new EffectBonus(
                newEffectField.getValue().trim(),
                Math.max(1, parseInt(newLevelField.getValue(), 1)),
                parseInt(newEffectDurationField.getValue(), 0));
    }

    private boolean hasPendingBonus() {
        return editable && (!newAttributeField.getValue().isBlank()
                || !newOperationField.getValue().isBlank()
                || !newAmountField.getValue().isBlank()
                || !newDurationField.getValue().isBlank());
    }

    private boolean hasPendingEffect() {
        return editable && (!newEffectField.getValue().isBlank()
                || !newLevelField.getValue().isBlank()
                || !newEffectDurationField.getValue().isBlank());
    }

    private void removeBonus(AttributeBonus bonus) {
        entry.attributes.remove(bonus);
        rebuildContent();
    }

    private void removeEffect(EffectBonus bonus) {
        entry.effects.remove(bonus);
        rebuildContent();
    }

    private void resetBonuses() {
        entry.attributes.clear();
        entry.effects.clear();
        if (icon != null) {
            entry.attributes.addAll(FoodItemEntry.baseline(presets, icon));
            entry.effects.addAll(FoodItemEntry.baselineEffects(presets, icon));
        }
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

    private void applyEffectRows() {
        for (EffectRow row : effectRows) {
            row.bonus().effect = row.effect().getValue().trim();
            row.bonus().level = Math.max(1, parseInt(row.level().getValue(), row.bonus().level));
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

    private boolean isValidAttribute(String value) {
        return ConsumableFoodInstance.resolveAttribute(value.trim()) != null;
    }

    private boolean isValidEffect(String value) {
        return EffectBonus.resolve(value.trim()) != null;
    }

    private boolean isValidOperation(String value) {
        String trimmed = value.trim();
        return OPERATIONS.stream().anyMatch(option -> option.value().equalsIgnoreCase(trimmed));
    }

    private boolean isValidAmount(String value) {
        try {
            return Double.parseDouble(value.trim()) != 0.0;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isValidLevel(String value) {
        try {
            return Integer.parseInt(value.trim()) >= 1;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isValidDuration(String value) {
        try {
            return Integer.parseInt(value.trim()) != 0;
        } catch (Exception exception) {
            return false;
        }
    }

    private @Nullable Component attributeTooltip(String value) {
        return isValidAttribute(value) ? null : Component.translatable("config.renourisheddelight.food_items.attribute_invalid");
    }

    private @Nullable Component effectTooltip(String value) {
        return isValidEffect(value) ? null : Component.translatable("config.renourisheddelight.food_items.effect_invalid");
    }

    private @Nullable Component operationTooltip(String value) {
        return isValidOperation(value) ? null : Component.translatable("config.renourisheddelight.food_items.modifier_invalid");
    }

    private @Nullable Component amountTooltip(String amountValue, String operationValue) {
        return formatAmount(amountValue, operationValue);
    }

    private @Nullable Component durationTooltip(String durationValue) {
        try {
            int ticks = Integer.parseInt(durationValue.trim());
            return Component.literal(StringUtil.formatTickDuration(ticks, 20));
        } catch (Exception exception) {
            return null;
        }
    }

    private @Nullable Component formatAmount(String amountValue, String operationValue) {
        double amount;
        try {
            amount = Double.parseDouble(amountValue.trim());
        } catch (Exception exception) {
            return null;
        }
        String operation = operationValue.trim().toLowerCase(Locale.ROOT);
        boolean percent = operation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName())
                || operation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName());
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

        for (EffectRow row : effectRows) {
            row.effect().setTextColor(isValidEffect(row.effect().getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
            row.level().setTextColor(isValidLevel(row.level().getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
            row.duration().setTextColor(isValidDuration(row.duration().getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        }

        newAttributeField.setTextColor(newAttributeField.getValue().isEmpty() || isValidAttribute(newAttributeField.getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
        newOperationField.setTextColor(newOperationField.getValue().isEmpty() || isValidOperation(newOperationField.getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
        newAmountField.setTextColor(newAmountField.getValue().isEmpty() || isValidAmount(newAmountField.getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        newDurationField.setTextColor(newDurationField.getValue().isEmpty() || isValidDuration(newDurationField.getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);

        newEffectField.setTextColor(newEffectField.getValue().isEmpty() || isValidEffect(newEffectField.getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
        newLevelField.setTextColor(newLevelField.getValue().isEmpty() || isValidLevel(newLevelField.getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        newEffectDurationField.setTextColor(newEffectDurationField.getValue().isEmpty() || isValidDuration(newEffectDurationField.getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private boolean isHovering(EditBox box, int mouseX, int mouseY) {
        return box.visible
                && mouseX >= box.getX() && mouseX < box.getX() + box.getWidth()
                && mouseY >= box.getY() && mouseY < box.getY() + box.getHeight();
    }

    private boolean isHoveringBarrier(EditBox anchor, int mouseX, int mouseY) {
        int x = anchor.getX() - 20;
        int y = anchor.getY() + 2;
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    private void renderFieldTooltip(EditBox box, @Nullable Component text, int mouseX, int mouseY) {
        if (text != null && isHovering(box, mouseX, mouseY)) {
            setTooltipForNextRenderPass(text);
        }
    }

    private void renderTooltips(int mouseX, int mouseY) {
        for (BonusRow row : rows) {
            String attributeValue = row.attribute().getValue();
            String operationValue = row.operation().getValue();
            String amountValue = row.amount().getValue();
            String durationValue = row.duration().getValue();

            boolean invalid = !isValidAttribute(attributeValue) || !isValidOperation(operationValue)
                    || !isValidAmount(amountValue) || !isValidDuration(durationValue);

            if (invalid && isHoveringBarrier(row.attribute(), mouseX, mouseY)) {
                setTooltipForNextRenderPass(Component.translatable("config.renourisheddelight.food_items.bonus_invalid"));
            }
            renderFieldTooltip(row.attribute(), attributeTooltip(attributeValue), mouseX, mouseY);
            renderFieldTooltip(row.operation(), operationTooltip(operationValue), mouseX, mouseY);
            renderFieldTooltip(row.amount(), amountTooltip(amountValue, operationValue), mouseX, mouseY);
            renderFieldTooltip(row.duration(), durationTooltip(durationValue), mouseX, mouseY);
        }

        for (EffectRow row : effectRows) {
            String effectValue = row.effect().getValue();
            boolean invalid = !isValidEffect(effectValue) || !isValidLevel(row.level().getValue()) || !isValidDuration(row.duration().getValue());

            if (invalid && isHoveringBarrier(row.effect(), mouseX, mouseY)) {
                setTooltipForNextRenderPass(Component.translatable("config.renourisheddelight.food_items.bonus_invalid"));
            }
            renderFieldTooltip(row.effect(), effectTooltip(effectValue), mouseX, mouseY);
            renderFieldTooltip(row.duration(), durationTooltip(row.duration().getValue()), mouseX, mouseY);
        }

        renderFieldTooltip(newAttributeField, newAttributeField.getValue().isEmpty() ? null : attributeTooltip(newAttributeField.getValue()), mouseX, mouseY);
        renderFieldTooltip(newOperationField, newOperationField.getValue().isEmpty() ? null : operationTooltip(newOperationField.getValue()), mouseX, mouseY);
        renderFieldTooltip(newAmountField, newAmountField.getValue().isEmpty() ? null : amountTooltip(newAmountField.getValue(), newOperationField.getValue()), mouseX, mouseY);
        renderFieldTooltip(newDurationField, newDurationField.getValue().isEmpty() ? null : durationTooltip(newDurationField.getValue()), mouseX, mouseY);

        renderFieldTooltip(newEffectField, newEffectField.getValue().isEmpty() ? null : effectTooltip(newEffectField.getValue()), mouseX, mouseY);
        renderFieldTooltip(newEffectDurationField, newEffectDurationField.getValue().isEmpty() ? null : durationTooltip(newEffectDurationField.getValue()), mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        applyValidationColors();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltips(mouseX, mouseY);
    }

    @Override
    protected void onDone() {
        applyRows();
        applyEffectRows();
        if (hasPendingBonus()) entry.attributes.add(pendingBonus());
        if (hasPendingEffect()) entry.effects.add(pendingEffect());
        saveAction.run();
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int subtitleTextY = SUBTITLE_Y + 4;

        Component itemText = Component.literal(entry.item).withStyle(ChatFormatting.GRAY);
        if (icon != null) {
            String name = icon.getDescription().getString();
            itemText = Component.literal(name).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(" (" + entry.item + ")").withStyle(ChatFormatting.GRAY));
            graphics.renderItem(new ItemStack(icon), centerX - 240, SUBTITLE_Y);
            graphics.drawString(font, itemText, centerX - 220, subtitleTextY, 0xFFFFFFFF);
        } else {
            graphics.drawString(font, itemText, centerX - 240, subtitleTextY, 0xFFFFFFFF);
        }

        applyRows();
        applyEffectRows();
        int maxDuration = Math.max(
                entry.attributes.stream().mapToInt(bonus -> bonus.duration).max().orElse(0),
                entry.effects.stream().mapToInt(bonus -> bonus.duration).max().orElse(0));
        Component durationText = Component.translatable("config.renourisheddelight.food_items.duration").copy().withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(StringUtil.formatTickDuration(maxDuration, 20)).withStyle(ChatFormatting.WHITE));
        graphics.drawString(font, durationText, centerX + 244 - font.width(durationText), subtitleTextY, 0xFFFFFF);

        if (onAttributeTab()) {
            graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.attribute"), centerX - 240, HEADER_LABEL_Y, 0xFFFFFF);
            graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.modifier"), centerX - 54, HEADER_LABEL_Y, 0xFFFFFF);
            graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.amount"), centerX + 92, HEADER_LABEL_Y, 0xFFFFFF);
        } else {
            graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.effect"), centerX - 240, HEADER_LABEL_Y, 0xFFFFFF);
            graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.level"), centerX + 92, HEADER_LABEL_Y, 0xFFFFFF);
        }
        graphics.drawString(font, Component.translatable("config.renourisheddelight.food_items.duration"), centerX + 158, HEADER_LABEL_Y, 0xFFFFFF);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (onAttributeTab() && entry.attributes.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.food_items.no_bonuses"), width / 2, height / 2, 0xAAAAAA);
        }
        if (!onAttributeTab() && entry.effects.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.food_items.no_effects"), width / 2, height / 2, 0xAAAAAA);
        }

        for (BonusRow row : rows) {
            boolean invalid = !isValidAttribute(row.attribute().getValue())
                    || !isValidOperation(row.operation().getValue())
                    || !isValidAmount(row.amount().getValue())
                    || !isValidDuration(row.duration().getValue());

            if (invalid) {
                graphics.renderItem(BARRIER_ICON, row.attribute().getX() - 20, row.attribute().getY() + 2);
            }
        }

        for (EffectRow row : effectRows) {
            boolean invalid = !isValidEffect(row.effect().getValue())
                    || !isValidLevel(row.level().getValue())
                    || !isValidDuration(row.duration().getValue());

            if (invalid) {
                graphics.renderItem(BARRIER_ICON, row.effect().getX() - 20, row.effect().getY() + 2);
            }
        }
    }

    private final class AttributeTab implements Tab {
        @Override
        public Component getTabTitle() {
            return Component.translatable("config.renourisheddelight.food_items.attributes");
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {
            // widgets are managed by rebuildContent
        }

        @Override
        public void doLayout(ScreenRectangle rectangle) {
            selectTab(TAB_ATTRIBUTES);
        }
    }

    private final class EffectTab implements Tab {
        @Override
        public Component getTabTitle() {
            return Component.translatable("config.renourisheddelight.food_items.effects");
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {
            // widgets are managed by rebuildContent
        }

        @Override
        public void doLayout(ScreenRectangle rectangle) {
            selectTab(TAB_EFFECTS);
        }
    }

    private record BonusRow(AttributeBonus bonus, EditBox attribute, EditBox operation, EditBox amount, EditBox duration, Button remove) {
        // do nothing
    }

    private record EffectRow(EffectBonus bonus, EditBox effect, EditBox level, EditBox duration, Button remove) {
        // do nothing
    }
}
