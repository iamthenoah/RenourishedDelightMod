package com.than00ber.renourisheddelight.compat.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
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
    private static final int LABEL_WIDTH = 190;
    private static final int MULTIPLIER_WIDTH = 60;
    private static final int HEADER_LABEL_Y = 76;
    private static final int LIST_TOP = 92;
    private static final int NORMAL_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xFF5555;

    private final @Nullable Screen parent;
    private final List<MultiplierRow> rows = new ArrayList<>();
    private String searchQuery = "";
    private boolean noResults;

    public DurationMultiplierScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.duration_multipliers"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        modFilterField = new ModFilterField(() -> listAttributes().stream().map(DurationMultiplierScreen::idOf).toList(), namespace -> rebuildContent());

        EditBox searchField = new EditBox(font, centerX - SIDE_MARGIN, 30, 170, 20, Component.translatable("config.renourisheddelight.duration_multipliers.search"));
        searchField.setMaxLength(256);
        searchField.setHint(Component.translatable("config.renourisheddelight.duration_multipliers.search_hint").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setResponder(value -> {
            searchQuery = value.toLowerCase(Locale.ROOT).trim();
            scrollOffset = 0;
            rebuildContent();
        });
        addViewWidget(searchField);

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
        applyRows();

        for (MultiplierRow row : rows) {
            removeWidget(row.multiplier());
        }
        rows.clear();

        int centerX = width / 2;
        int listBottom = height - 68;
        int rowGap = ROW_HEIGHT - 20;
        int visibleRows = Math.max(1, (listBottom - LIST_TOP + rowGap) / ROW_HEIGHT);

        modFilterField.rebuild(centerX + 35, 30, 110, 20, Component.translatable("config.renourisheddelight.filter"));

        List<Attribute> filtered = listAttributes().stream()
                .filter(attribute -> modFilterField.matches(idOf(attribute)))
                .filter(this::matchesSearch)
                .toList();
        noResults = filtered.isEmpty();
        scrollMaxOffset = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, filtered.size());

        scrollTrackX = centerX + SIDE_MARGIN + 10;
        scrollTrackTop = LIST_TOP;
        scrollTrackBottom = listBottom;

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            Attribute attribute = filtered.get(i + scrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;

            EditBox multiplierField = new EditBox(font, centerX - SIDE_MARGIN + LABEL_WIDTH + 5, y, MULTIPLIER_WIDTH, 20, Component.translatable("config.renourisheddelight.duration_multipliers.multiplier"));
            multiplierField.setMaxLength(32);
            multiplierField.setValue(String.valueOf(config.multiplier(idOf(attribute))));
            addRenderableWidget(multiplierField);

            rows.add(new MultiplierRow(idOf(attribute), nameOf(attribute), y, multiplierField));
        }
    }

    private List<Attribute> listAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        BuiltInRegistries.ATTRIBUTE.forEach(attributes::add);
        attributes.sort(Comparator.comparing(DurationMultiplierScreen::idOf, String.CASE_INSENSITIVE_ORDER));
        return attributes;
    }

    private boolean matchesSearch(Attribute attribute) {
        if (searchQuery.isEmpty()) return true;
        return (idOf(attribute) + " " + nameOf(attribute)).toLowerCase(Locale.ROOT).contains(searchQuery);
    }

    private static String idOf(Attribute attribute) {
        return BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString();
    }

    private static String nameOf(Attribute attribute) {
        return Component.translatable(attribute.getDescriptionId()).getString();
    }

    private void applyRows() {
        if (!editable) return;

        for (MultiplierRow row : rows) {
            config.setMultiplier(row.attribute(), parseDouble(row.multiplier().getValue(), 1.0));
        }
    }

    private void resetAll() {
        if (!editable) return;
        config.multipliers.clear();
        save();
        scrollOffset = 0;
        rebuildContent();
    }

    private double parseDouble(String value, double fallback) {
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed > 0.0 ? parsed : fallback;
        } catch (Exception exception) {
            return fallback;
        }
    }

    private boolean isValid(String value) {
        try {
            return Double.parseDouble(value.trim()) > 0.0;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (MultiplierRow row : rows) {
            row.multiplier().setTextColor(isValid(row.multiplier().getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void onDone() {
        applyRows();
        save();
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = width / 2 - SIDE_MARGIN;
        graphics.drawString(font, Component.translatable("config.renourisheddelight.duration_multipliers.attribute"), left, HEADER_LABEL_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.duration_multipliers.multiplier"), left + LABEL_WIDTH + 5, HEADER_LABEL_Y, 0xFFFFFF);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = width / 2 - SIDE_MARGIN;

        for (MultiplierRow row : rows) {
            String label = font.plainSubstrByWidth(row.name(), LABEL_WIDTH - 4);
            graphics.drawString(font, label, left, row.y() + 6, 0xE0E0E0);
        }
        if (noResults) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.duration_multipliers.no_results"), width / 2, height / 2, 0xAAAAAA);
        }
    }

    private record MultiplierRow(String attribute, String name, int y, EditBox multiplier) {
        // do nothing
    }
}
