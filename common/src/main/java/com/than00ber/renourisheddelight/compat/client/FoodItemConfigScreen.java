package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class FoodItemConfigScreen extends AbstractFoodConfigScreen {

    private static final int SIDE_MARGIN = 140;

    private final @Nullable Screen parent;
    private final List<IconEntry> icons = new ArrayList<>();
    private final List<AbstractWidget> rowWidgets = new ArrayList<>();

    private EditBox newItemField;
    private String searchQuery = "";
    private boolean noResults;

    public FoodItemConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.food_items"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int left = centerX - SIDE_MARGIN;

        modFilterField = new ModFilterField(() -> listItems().stream().map(FoodItemConfigScreen::idOf).toList(), namespace -> rebuildContent());

        EditBox searchField = new EditBox(font, left, 30, 170, 20, Component.translatable("config.renourisheddelight.food_items.search"));
        searchField.setMaxLength(256);
        searchField.setHint(Component.translatable("config.renourisheddelight.food_items.search_hint").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setResponder(value -> {
            searchQuery = value.toLowerCase(Locale.ROOT).trim();
            scrollOffset = 0;
            rebuildContent();
        });
        addViewWidget(searchField);

        newItemField = new EditBox(font, left, height - 56, 260, 20, Component.translatable("config.renourisheddelight.food_items.new_item"));
        newItemField.setMaxLength(256);
        newItemField.setHint(Component.literal("minecraft:cake").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newItemField);
        suggestFields.add(new SuggestField(newItemField, buildItemOptions(), true));

        addRenderableWidget(Button.builder(Component.literal("+"), button -> addItem())
                .bounds(centerX + 125, height - 56, 20, 20)
                .build());

        int buttonsY = height - 28;
        int buttonsWidth = 200;
        int buttonsLeft = centerX - buttonsWidth / 2;
        int gap = 5;
        int halfWidth = (buttonsWidth - gap) / 2;

        addViewWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(buttonsLeft, buttonsY, halfWidth, 20)
                .build());
        addRenderableWidget(createResetButton(buttonsLeft + halfWidth + gap, buttonsY, buttonsWidth - halfWidth - gap, 20, this::resetAll));
        rebuildContent();
    }

    @Override
    protected void rebuildContent() {
        for (AbstractWidget widget : rowWidgets) {
            removeWidget(widget);
        }
        rowWidgets.clear();
        icons.clear();

        int centerX = width / 2;
        modFilterField.rebuild(centerX + 35, 30, 110, 20, Component.translatable("config.renourisheddelight.filter"));

        List<Item> filtered = listItems().stream()
                .filter(item -> modFilterField.matches(idOf(item)))
                .filter(this::matchesSearch)
                .toList();
        noResults = filtered.isEmpty();

        int listTop = 76;
        int listBottom = height - 68;
        int rowGap = ROW_HEIGHT - 17;
        int visibleRows = Math.max(1, (listBottom - listTop + rowGap) / ROW_HEIGHT);
        scrollMaxOffset = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, filtered.size());

        int iconX = centerX - SIDE_MARGIN;
        int nameX = iconX + 20;
        int nameWidth = 241;
        int resetX = centerX + 125;
        scrollTrackX = centerX + 150;
        scrollTrackTop = listTop;
        scrollTrackBottom = listBottom;

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            Item item = filtered.get(i + scrollOffset);
            int y = listTop + i * ROW_HEIGHT;
            boolean customized = config.entry(item) != null;
            icons.add(new IconEntry(new ItemStack(item), iconX, y + 2));

            MutableComponent label = item.getDescription().copy();
            if (customized) label = label.withStyle(ChatFormatting.YELLOW);

            Button nameButton = Button.builder(label, button -> openBonuses(item))
                    .bounds(nameX, y, nameWidth, 20)
                    .tooltip(buildTooltip(item))
                    .build();
            addRenderableWidget(nameButton);
            rowWidgets.add(nameButton);

            Button resetButton = Button.builder(Component.literal("x"), button -> resetItem(item))
                    .bounds(resetX, y, 20, 20)
                    .tooltip(Tooltip.create(Component.translatable("config.renourisheddelight.food_items.reset_item")))
                    .build();
            resetButton.active = customized && editable;
            addRenderableWidget(resetButton);
            rowWidgets.add(resetButton);
        }
    }

    private List<Item> listItems() {
        List<Item> items = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR && item.components().get(DataComponents.FOOD) != null) items.add(item);
        }
        for (FoodItemEntry entry : config.foods) {
            Item item = resolveItem(entry.item);
            if (item != null && !items.contains(item)) items.add(item);
        }
        items.sort(Comparator.comparing(FoodItemConfigScreen::idOf, String.CASE_INSENSITIVE_ORDER));
        return items;
    }

    private boolean matchesSearch(Item item) {
        if (searchQuery.isEmpty()) return true;
        return (idOf(item) + " " + item.getDescription().getString()).toLowerCase(Locale.ROOT).contains(searchQuery);
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
        Holder<Attribute> attribute = ConsumableFoodInstance.resolveAttribute(bonus.attribute);
        Component name = attribute != null
                ? Component.translatable(attribute.value().getDescriptionId())
                : Component.literal(bonus.attribute);

        AttributeModifier.Operation operation = resolveOperation(bonus.operation);
        double display = operation != AttributeModifier.Operation.ADD_VALUE ? bonus.amount * 100.0 : bonus.amount;
        int effective = config.effectiveDuration(bonus);
        String durationText = StringUtil.formatTickDuration(bonus.duration, 20);

        Component durationComponent = effective != bonus.duration
                ? Component.literal(durationText + " -> " + StringUtil.formatTickDuration(effective, 20)).withStyle(ChatFormatting.GOLD)
                : Component.literal(durationText);
        Component amountLine = display >= 0
                ? Component.translatable("attribute.modifier.plus." + operation.id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(display), name)
                : Component.translatable("attribute.modifier.take." + operation.id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-display), name);
        return Component.empty().append(amountLine).append(" (").append(durationComponent).append(")");
    }

    private AttributeModifier.Operation resolveOperation(@Nullable String raw) {
        String value = raw != null ? raw.trim().toLowerCase(Locale.ROOT) : "";

        for (AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
            if (operation.getSerializedName().equals(value)) return operation;
        }
        return AttributeModifier.Operation.ADD_VALUE;
    }

    private static String idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static @Nullable Item resolveItem(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            return item != Items.AIR ? item : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private List<SuggestOption> buildItemOptions() {
        List<SuggestOption> options = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item == Items.AIR) return;
            String id = idOf(item);
            String name = item.getDescription().getString();
            options.add(new SuggestOption(id, name, (id + " " + name).toLowerCase(Locale.ROOT)));
        });
        options.sort(Comparator.comparing(SuggestOption::value, String.CASE_INSENSITIVE_ORDER));
        return options;
    }

    private void addItem() {
        if (!editable) return;
        Item item = resolveItem(newItemField.getValue().trim());
        if (item == null) return;
        newItemField.setValue("");
        openBonuses(item);
    }

    private void openBonuses(Item item) {
        if (!editable) return;
        minecraft.setScreen(new FoodItemBonusScreen(this, config.claim(item), () -> {
            config.prune(item);
            save();
        }));
    }

    private void resetItem(Item item) {
        if (!editable) return;
        config.reset(item);
        save();
        rebuildContent();
    }

    private void resetAll() {
        if (!editable) return;
        config.foods.clear();
        save();
        scrollOffset = 0;
        rebuildContent();
    }

    @Override
    protected void onDone() {
        save();
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (IconEntry icon : icons) {
            graphics.renderItem(icon.stack(), icon.x(), icon.y());
        }
        if (noResults) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.food_items.no_results"), width / 2, height / 2, 0xAAAAAA);
        }
    }

    private record IconEntry(ItemStack stack, int x, int y) {
    }
}
