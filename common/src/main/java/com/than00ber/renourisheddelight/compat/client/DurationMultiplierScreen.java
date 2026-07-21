package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.DurationMultiplierEntry;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class DurationMultiplierScreen extends AbstractFoodConfigScreen {

    private static final int SIDE_MARGIN = 140;
    private static final int ATTRIBUTE_WIDTH = 190;
    private static final int MULTIPLIER_WIDTH = 60;
    private static final int NORMAL_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xFF5555;
    private static final int ORANGE_TEXT_COLOR = 0xFFAA00;

    private final @Nullable Screen parent;
    private final List<DurationMultiplierEntry> workingEntries;
    private final List<MultiplierRow> rows = new ArrayList<>();
    private List<SuggestOption> attributeOptions = List.of();
    private String searchQuery = "";

    private EditBox newAttributeField;
    private EditBox newMultiplierField;

    public DurationMultiplierScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.duration_multipliers"));
        this.parent = parent;
        this.workingEntries = CommonConfiguration.getInstance().durationMultipliers;
    }

    @Override
    protected void init() {
        attributeOptions = buildAttributeOptions();
        int centerX = width / 2;
        int newRowY = height - 56;

        modFilterField = new ModFilterField(() -> workingEntries.stream().map(entry -> entry.attribute).toList(), namespace -> rebuildContent());

        EditBox searchField = new EditBox(font, centerX - SIDE_MARGIN, 30, 170, 20, Component.translatable("config.renourisheddelight.duration_multipliers.search"));
        searchField.setMaxLength(256);
        searchField.setHint(Component.translatable("config.renourisheddelight.duration_multipliers.search_hint").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setResponder(value -> {
            searchQuery = value.toLowerCase(Locale.ROOT).trim();
            scrollOffset = 0;
            rebuildContent();
        });
        addRenderableWidget(searchField);

        newAttributeField = new EditBox(font, centerX - SIDE_MARGIN, newRowY, ATTRIBUTE_WIDTH, 20, Component.translatable("config.renourisheddelight.duration_multipliers.attribute"));
        newAttributeField.setMaxLength(256);
        newAttributeField.setHint(Component.translatable("config.renourisheddelight.duration_multipliers.attribute").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newAttributeField);
        suggestFields.add(new SuggestField(newAttributeField, attributeOptions, true));

        newMultiplierField = new EditBox(font, centerX - SIDE_MARGIN + ATTRIBUTE_WIDTH + 5, newRowY, MULTIPLIER_WIDTH, 20, Component.translatable("config.renourisheddelight.duration_multipliers.multiplier"));
        newMultiplierField.setMaxLength(32);
        newMultiplierField.setHint(Component.translatable("config.renourisheddelight.duration_multipliers.multiplier").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newMultiplierField);

        addRenderableWidget(Button.builder(Component.literal("+"), button -> addMultiplier())
                .bounds(centerX + SIDE_MARGIN - 20, newRowY, 20, 20)
                .build());

        int buttonsY = height - 28;
        int buttonsWidth = 200;
        int buttonsLeft = centerX - buttonsWidth / 2;
        int gap = 5;
        int halfWidth = (buttonsWidth - gap) / 2;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(buttonsLeft, buttonsY, halfWidth, 20)
                .build());
        addRenderableWidget(createResetButton(buttonsLeft + halfWidth + gap, buttonsY, buttonsWidth - halfWidth - gap, 20, this::resetMultipliers));
        rebuildContent();
    }

    @Override
    protected void rebuildContent() {
        applyRows();

        for (MultiplierRow row : rows) {
            removeWidget(row.attribute());
            removeWidget(row.multiplier());
            removeWidget(row.remove());
        }
        rows.clear();
        suggestFields.removeIf(field -> field.box != newAttributeField);

        int centerX = width / 2;
        int listTop = 80;
        int listBottom = height - 68;
        int rowGap = ROW_HEIGHT - 20;
        int visibleRows = Math.max(1, (listBottom - listTop + rowGap) / ROW_HEIGHT);

        modFilterField.rebuild(centerX + 35, 30, 110, 20, Component.translatable("config.renourisheddelight.filter"));

        List<DurationMultiplierEntry> filtered = workingEntries.stream()
                .filter(entry -> modFilterField.matches(entry.attribute != null ? entry.attribute : ""))
                .filter(this::matchesSearch)
                .toList();
        scrollMaxOffset = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, filtered.size());

        scrollTrackX = centerX + SIDE_MARGIN + 10;
        scrollTrackTop = listTop;
        scrollTrackBottom = listBottom;

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            DurationMultiplierEntry entry = filtered.get(i + scrollOffset);
            int y = listTop + i * ROW_HEIGHT;

            EditBox attributeField = new EditBox(font, centerX - SIDE_MARGIN, y, ATTRIBUTE_WIDTH, 20, Component.translatable("config.renourisheddelight.duration_multipliers.attribute"));
            attributeField.setMaxLength(256);
            attributeField.setValue(entry.attribute != null ? entry.attribute : "");
            attributeField.setHint(Component.translatable("config.renourisheddelight.duration_multipliers.attribute").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(attributeField);
            suggestFields.add(new SuggestField(attributeField, attributeOptions));

            EditBox multiplierField = new EditBox(font, centerX - SIDE_MARGIN + ATTRIBUTE_WIDTH + 5, y, MULTIPLIER_WIDTH, 20, Component.translatable("config.renourisheddelight.duration_multipliers.multiplier"));
            multiplierField.setMaxLength(32);
            multiplierField.setValue(String.valueOf(entry.multiplier));
            multiplierField.setHint(Component.translatable("config.renourisheddelight.duration_multipliers.multiplier").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(multiplierField);

            Button removeButton = Button.builder(Component.literal("x"), button -> removeMultiplier(entry))
                    .bounds(centerX + SIDE_MARGIN - 20, y, 20, 20)
                    .build();
            addRenderableWidget(removeButton);

            rows.add(new MultiplierRow(entry, attributeField, multiplierField, removeButton));
        }
    }

    private boolean matchesSearch(DurationMultiplierEntry entry) {
        if (searchQuery.isEmpty()) return true;
        String id = entry.attribute != null ? entry.attribute.toLowerCase(Locale.ROOT) : "";
        if (id.contains(searchQuery)) return true;

        Holder<Attribute> attribute = ConsumableFoodInstance.resolveAttribute(entry.attribute);
        if (attribute == null) return false;

        String name = Component.translatable(attribute.value().getDescriptionId()).getString().toLowerCase(Locale.ROOT);
        return name.contains(searchQuery);
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

    private void addMultiplier() {
        String attribute = newAttributeField.getValue().trim();
        double multiplier = parseDouble(newMultiplierField.getValue(), 1.0);
        if (attribute.isEmpty()) return;

        DurationMultiplierEntry existing = findEntry(attribute);
        if (existing != null) {
            existing.multiplier = multiplier;
        } else {
            workingEntries.add(new DurationMultiplierEntry(attribute, multiplier));
        }
        scrollOffset = Integer.MAX_VALUE;

        newAttributeField.setValue("");
        newMultiplierField.setValue("");

        saveWorkingEntries();
        rebuildContent();
    }

    private @Nullable DurationMultiplierEntry findEntry(String attribute) {
        Holder<Attribute> resolved = ConsumableFoodInstance.resolveAttribute(attribute);
        if (resolved == null) return null;

        for (DurationMultiplierEntry entry : workingEntries) {
            Holder<Attribute> candidate = ConsumableFoodInstance.resolveAttribute(entry.attribute);
            if (candidate != null && candidate.value() == resolved.value()) {
                return entry;
            }
        }
        return null;
    }

    private void removeMultiplier(DurationMultiplierEntry entry) {
        workingEntries.remove(entry);
        saveWorkingEntries();
        rebuildContent();
    }

    private void resetMultipliers() {
        workingEntries.clear();
        CommonConfiguration.getInstance().populateDurationMultiplierDefaults();
        saveWorkingEntries();
        scrollOffset = 0;
        rebuildContent();
    }

    private void applyRows() {
        for (MultiplierRow row : rows) {
            row.entry().attribute = row.attribute().getValue().trim();
            row.entry().multiplier = parseDouble(row.multiplier().getValue(), row.entry().multiplier);
        }
    }

    private void saveWorkingEntries() {
        AutoConfig.getConfigHolder(CommonConfiguration.class).save();
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private boolean isValidAttribute(String value) {
        return ConsumableFoodInstance.resolveAttribute(value.trim()) != null;
    }

    private boolean isValidMultiplier(String value) {
        try {
            return Double.parseDouble(value.trim()) > 0.0;
        } catch (Exception exception) {
            return false;
        }
    }

    private @Nullable Component attributeTooltip(String value) {
        return isValidAttribute(value) ? null : Component.translatable("config.renourisheddelight.food_items.attribute_invalid");
    }

    private @Nullable Component multiplierTooltip(String value) {
        return isValidMultiplier(value) ? null : Component.translatable("config.renourisheddelight.duration_multipliers.multiplier_invalid");
    }

    private void applyValidationColors() {
        for (MultiplierRow row : rows) {
            row.attribute().setTextColor(isValidAttribute(row.attribute().getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
            row.multiplier().setTextColor(isValidMultiplier(row.multiplier().getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        }
        newAttributeField.setTextColor(newAttributeField.getValue().isEmpty() || isValidAttribute(newAttributeField.getValue()) ? NORMAL_TEXT_COLOR : ORANGE_TEXT_COLOR);
        newMultiplierField.setTextColor(newMultiplierField.getValue().isEmpty() || isValidMultiplier(newMultiplierField.getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private boolean isHovering(EditBox box, int mouseX, int mouseY) {
        return mouseX >= box.getX() && mouseX < box.getX() + box.getWidth()
                && mouseY >= box.getY() && mouseY < box.getY() + box.getHeight();
    }

    private void renderFieldTooltip(EditBox box, @Nullable Component text, int mouseX, int mouseY) {
        if (text != null && isHovering(box, mouseX, mouseY)) {
            setTooltipForNextRenderPass(text);
        }
    }

    private void renderTooltips(int mouseX, int mouseY) {
        for (MultiplierRow row : rows) {
            renderFieldTooltip(row.attribute(), attributeTooltip(row.attribute().getValue()), mouseX, mouseY);
            renderFieldTooltip(row.multiplier(), multiplierTooltip(row.multiplier().getValue()), mouseX, mouseY);
        }
        renderFieldTooltip(newAttributeField, newAttributeField.getValue().isEmpty() ? null : attributeTooltip(newAttributeField.getValue()), mouseX, mouseY);
        renderFieldTooltip(newMultiplierField, newMultiplierField.getValue().isEmpty() ? null : multiplierTooltip(newMultiplierField.getValue()), mouseX, mouseY);
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
        saveWorkingEntries();
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        graphics.drawString(font, Component.translatable("config.renourisheddelight.duration_multipliers.attribute"), centerX - SIDE_MARGIN, 64, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.duration_multipliers.multiplier"), centerX - SIDE_MARGIN + ATTRIBUTE_WIDTH + 5, 64, 0xFFFFFF);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (workingEntries.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.duration_multipliers.empty"), width / 2, height / 2, 0xAAAAAA);
        } else if (rows.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.duration_multipliers.no_results"), width / 2, height / 2, 0xAAAAAA);
        }
    }

    private record MultiplierRow(DurationMultiplierEntry entry, EditBox attribute, EditBox multiplier, Button remove) {
    }
}
