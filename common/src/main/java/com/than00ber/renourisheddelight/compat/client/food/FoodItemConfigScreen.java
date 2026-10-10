package com.than00ber.renourisheddelight.compat.client.food;

import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.food.AttributeOperation;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FoodItemConfigScreen extends AbstractFoodConfigScreen {

    private static final int SIDE_MARGIN = 140;

    private final List<IconEntry> icons = new ArrayList<>();
    private final List<AbstractWidget> rowWidgets = new ArrayList<>();

    private List<Item> items = List.of();
    private EditBox newItemField;
    private boolean noResults;

    public FoodItemConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.food_items"), parent);
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int left = centerX - SIDE_MARGIN;
        modFilterField = new ModFilterField(() -> items.stream().map(FoodItemConfigScreen::idOf).toList(), namespace -> rebuildContent());

        addSearchField(left, "config.renourisheddelight.food_items");

        newItemField = addField(left, inputRowY(), 260, "config.renourisheddelight.food_items.new_item", 256);
        newItemField.setHint(Component.literal("minecraft:cake").withStyle(ChatFormatting.DARK_GRAY));
        suggestFields.add(new SuggestField(newItemField, buildItemOptions(), true));
        addSmallButton("+", centerX + 125, inputRowY(), this::addItem);

        addFooterButtons(this::resetAll);
        rebuildContent();
    }

    @Override
    protected void rebuildContent() {
        items = listItems();
        for (AbstractWidget widget : rowWidgets) {
            removeWidget(widget);
        }
        rowWidgets.clear();
        icons.clear();
        rebuildModFilter();

        int centerX = width / 2;
        int iconX = centerX - SIDE_MARGIN;
        int nameX = iconX + 20;
        int nameWidth = 241;
        int resetX = centerX + 125;
        List<Item> filtered = items.stream()
                .filter(item -> modFilterField.matches(idOf(item)))
                .filter(item -> matchesSearch(idOf(item) + " " + item.getDescription().getString()))
                .toList();
        noResults = filtered.isEmpty();
        int visibleRows = layoutList(HEADER_LABEL_Y, 17, filtered.size(), centerX + 150);

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            Item item = filtered.get(i + scrollOffset);
            int y = HEADER_LABEL_Y + i * ROW_HEIGHT;
            boolean customized = config.isCustomized(presets, item);
            icons.add(new IconEntry(new ItemStack(item), iconX, y + 2));

            MutableComponent label = item.getDescription().copy();
            if (customized) label = label.withStyle(ChatFormatting.YELLOW);

            Button nameButton = Button.builder(label, button -> openBonuses(item))
                    .bounds(nameX, y, nameWidth, BUTTON_HEIGHT)
                    .tooltip(buildTooltip(item))
                    .build();
            addRenderableWidget(nameButton);
            rowWidgets.add(nameButton);

            Button resetButton = addSmallButton("x", resetX, y, () -> resetItem(item));
            resetButton.setTooltip(Tooltip.create(Component.translatable("config.renourisheddelight.food_items.reset_item")));
            resetButton.active = customized && editable;
            rowWidgets.add(resetButton);
        }
    }

    private List<Item> listItems() {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR && item.isEdible()) items.add(item);
        }
        collect(items, config.foods);
        collect(items, presets);
        items.sort(Comparator.comparing(FoodItemConfigScreen::idOf, String.CASE_INSENSITIVE_ORDER));
        return items;
    }

    private static void collect(List<Item> items, List<FoodItemEntry> entries) {
        for (FoodItemEntry entry : entries) {
            Item item = resolveItem(entry.item);
            if (item != null && !items.contains(item)) items.add(item);
        }
    }

    private Tooltip buildTooltip(Item item) {
        List<AttributeBonus> bonuses = config.bonuses(item);
        if (bonuses.isEmpty()) {
            return Tooltip.create(Component.translatable("config.renourisheddelight.food_items.no_bonuses"));
        }
        MutableComponent text = Component.empty();
        for (int i = 0; i < bonuses.size(); i++) {
            if (i > 0) text.append("\n");
            text.append(formatBonusLine(bonuses.get(i)));
        }
        return Tooltip.create(text);
    }

    private Component formatBonusLine(AttributeBonus bonus) {
        Attribute attribute = ConsumableFoodInstance.resolveAttribute(bonus.attribute);
        Component name = attribute != null ? Component.translatable(attribute.getDescriptionId()) : Component.literal(bonus.attribute);
        AttributeModifier.Operation operation = AttributeOperation.parse(bonus.operation);
        double display = operation != AttributeModifier.Operation.ADDITION ? bonus.amount * 100.0 : bonus.amount;
        int effective = config.effectiveDuration(bonus);
        String durationText = StringUtil.formatTickDuration(bonus.duration);

        Component durationComponent = effective != bonus.duration
                ? Component.literal(durationText + " -> " + StringUtil.formatTickDuration(effective)).withStyle(ChatFormatting.GOLD)
                : Component.literal(durationText);
        Component amountLine = display >= 0
                ? Component.translatable("attribute.modifier.plus." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(display), name)
                : Component.translatable("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(-display), name);
        return Component.empty().append(amountLine).append(" (").append(durationComponent).append(")");
    }

    private static String idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private List<SuggestOption> buildItemOptions() {
        return buildOptions(BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR), FoodItemConfigScreen::idOf, item -> item.getDescription().getString());
    }

    private void addItem() {
        Item item = pendingItem();
        if (item == null) return;
        newItemField.setValue("");
        config.claim(presets, item);
        openBonuses(item);
    }

    private @Nullable Item pendingItem() {
        return resolveItem(newItemField.getValue().trim());
    }

    private static @Nullable Item resolveItem(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            return item != Items.AIR ? item : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private void openBonuses(Item item) {
        minecraft.setScreen(new FoodItemBonusScreen(this, item));
    }

    private void resetItem(Item item) {
        config.reset(item);
        rebuildContent();
    }

    private void resetAll() {
        config.foods.clear();
        scrollOffset = 0;
        rebuildContent();
    }

    @Override
    protected void save() {
        Item pending = pendingItem();
        if (pending != null) config.claim(presets, pending);
        holder.setFoodConfig(config);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (IconEntry icon : icons) {
            graphics.renderItem(icon.stack(), icon.x(), icon.y());
        }
        if (noResults) {
            drawCenteredMessage(graphics, "config.renourisheddelight.food_items.no_results");
        }
    }

    private record IconEntry(ItemStack stack, int x, int y) {
    }
}
