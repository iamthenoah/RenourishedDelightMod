package com.than00ber.renourisheddelight.compat.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DurationMultiplierScreen extends AbstractFoodConfigScreen {

    private static final int SIDE_MARGIN = 140;
    private static final int LABEL_WIDTH = 190;
    private static final int MULTIPLIER_WIDTH = 60;

    private final List<MultiplierRow> rows = new ArrayList<>();
    private List<Attribute> attributes = List.of();
    private boolean noResults;

    public DurationMultiplierScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.duration_multipliers"), parent);
    }

    @Override
    protected void init() {
        attributes = BuiltInRegistries.ATTRIBUTE.stream()
                .sorted(Comparator.comparing(DurationMultiplierScreen::idOf, String.CASE_INSENSITIVE_ORDER))
                .toList();
        modFilterField = new ModFilterField(() -> attributes.stream().map(DurationMultiplierScreen::idOf).toList(), namespace -> rebuildContent());

        addSearchField(width / 2 - SIDE_MARGIN, "config.renourisheddelight.duration_multipliers");
        addFooterButtons(this::resetAll);
        rebuildContent();
    }

    @Override
    protected void rebuildContent() {
        applyRows();
        for (MultiplierRow row : rows) {
            removeWidget(row.multiplier());
        }
        rows.clear();
        rebuildModFilter();

        int left = width / 2 - SIDE_MARGIN;
        List<Attribute> filtered = attributes.stream()
                .filter(attribute -> modFilterField.matches(idOf(attribute)))
                .filter(attribute -> matchesSearch(idOf(attribute) + " " + nameOf(attribute)))
                .toList();
        noResults = filtered.isEmpty();
        int visibleRows = layoutList(LIST_TOP, BUTTON_HEIGHT, filtered.size(), width / 2 + SIDE_MARGIN + 10);

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            Attribute attribute = filtered.get(i + scrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;
            EditBox multiplierField = addField(left + LABEL_WIDTH + GAP, y, MULTIPLIER_WIDTH, "config.renourisheddelight.duration_multipliers.multiplier", 32);
            multiplierField.setValue(String.valueOf(config.multiplier(idOf(attribute))));
            rows.add(new MultiplierRow(idOf(attribute), nameOf(attribute), y, multiplierField));
        }
    }

    private static String idOf(Attribute attribute) {
        return BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString();
    }

    private static String nameOf(Attribute attribute) {
        return Component.translatable(attribute.getDescriptionId()).getString();
    }

    private void applyRows() {
        for (MultiplierRow row : rows) {
            double fallback = config.multiplier(row.attribute());
            double parsed = parseDouble(row.multiplier().getValue(), fallback);
            config.setMultiplier(row.attribute(), parsed > 0.0 ? parsed : fallback);
        }
    }

    private void resetAll() {
        config.multipliers.clear();
        scrollOffset = 0;
        rebuildContent();
    }

    private boolean isValid(String value) {
        return parseDouble(value, 0.0) > 0.0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (MultiplierRow row : rows) {
            row.multiplier().setTextColor(isValid(row.multiplier().getValue()) ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void save() {
        applyRows();
        holder.setFoodConfig(config);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = width / 2 - SIDE_MARGIN;
        drawHeaderLabel(graphics, "config.renourisheddelight.duration_multipliers.attribute", left);
        drawHeaderLabel(graphics, "config.renourisheddelight.duration_multipliers.multiplier", left + LABEL_WIDTH + GAP);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = width / 2 - SIDE_MARGIN;
        for (MultiplierRow row : rows) {
            graphics.drawString(font, font.plainSubstrByWidth(row.name(), LABEL_WIDTH - 4), left, row.y() + 6, NORMAL_TEXT_COLOR);
        }
        if (noResults) {
            drawCenteredMessage(graphics, "config.renourisheddelight.duration_multipliers.no_results");
        }
    }

    private record MultiplierRow(String attribute, String name, int y, EditBox multiplier) {
    }
}
