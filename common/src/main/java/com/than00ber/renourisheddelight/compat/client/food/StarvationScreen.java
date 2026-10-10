package com.than00ber.renourisheddelight.compat.client.food;

import com.than00ber.renourisheddelight.config.data.StarvationEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StarvationScreen extends AbstractFoodConfigScreen {

    private static final int SIDE_MARGIN = 160;
    private static final int EFFECT_WIDTH = 160;
    private static final int NUMBER_WIDTH = 40;

    private final List<StageRow> rows = new ArrayList<>();
    private List<SuggestOption> effectOptions = List.of();

    private EditBox newEffectField;
    private EditBox newAfterField;
    private EditBox newAmplifierField;
    private EditBox newMaxField;

    public StarvationScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.starvation"), parent);
    }

    @Override
    protected void init() {
        effectOptions = buildOptions(BuiltInRegistries.MOB_EFFECT.stream(), effect -> BuiltInRegistries.MOB_EFFECT.getKey(effect).toString(), effect -> effect.getDisplayName().getString());
        int left = width / 2 - SIDE_MARGIN;
        int newRowY = inputRowY();
        modFilterField = new ModFilterField(() -> config.starvation.stream().map(entry -> entry.effect).toList(), namespace -> rebuildContent());

        addSearchField(left, "config.renourisheddelight.starvation");

        newEffectField = addField(left, newRowY, EFFECT_WIDTH, "config.renourisheddelight.starvation.effect", 256);
        suggestFields.add(new SuggestField(newEffectField, effectOptions, true));
        newAfterField = addField(left + EFFECT_WIDTH + GAP, newRowY, NUMBER_WIDTH, "config.renourisheddelight.starvation.after", 32);
        newAmplifierField = addField(left + EFFECT_WIDTH + NUMBER_WIDTH + GAP * 2, newRowY, NUMBER_WIDTH, "config.renourisheddelight.starvation.amplifier", 32);
        newMaxField = addField(left + EFFECT_WIDTH + NUMBER_WIDTH * 2 + GAP * 3, newRowY, NUMBER_WIDTH, "config.renourisheddelight.starvation.max", 32);
        addSmallButton("+", width / 2 + SIDE_MARGIN - BUTTON_HEIGHT, newRowY, this::addStage);

        addFooterButtons(this::resetStages);
        rebuildContent();
    }

    @Override
    protected void rebuildContent() {
        applyRows();
        for (StageRow row : rows) {
            removeWidget(row.effect());
            removeWidget(row.after());
            removeWidget(row.amplifier());
            removeWidget(row.max());
            removeWidget(row.remove());
        }
        rows.clear();
        suggestFields.removeIf(field -> field.box != newEffectField);
        rebuildModFilter();

        int left = width / 2 - SIDE_MARGIN;
        List<StarvationEntry> filtered = config.starvation.stream()
                .filter(entry -> modFilterField.matches(entry.effect != null ? entry.effect : ""))
                .filter(this::matchesSearch)
                .sorted(Comparator.comparingInt(entry -> entry.after))
                .toList();
        int visibleRows = layoutList(LIST_TOP, BUTTON_HEIGHT, filtered.size(), width / 2 + SIDE_MARGIN + 10);

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            StarvationEntry entry = filtered.get(i + scrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;

            EditBox effectField = addField(left, y, EFFECT_WIDTH, "config.renourisheddelight.starvation.effect", 256);
            effectField.setValue(entry.effect != null ? entry.effect : "");
            suggestFields.add(new SuggestField(effectField, effectOptions));

            EditBox afterField = addField(left + EFFECT_WIDTH + GAP, y, NUMBER_WIDTH, "config.renourisheddelight.starvation.after", 32);
            afterField.setValue(String.valueOf(entry.after));

            EditBox amplifierField = addField(left + EFFECT_WIDTH + NUMBER_WIDTH + GAP * 2, y, NUMBER_WIDTH, "config.renourisheddelight.starvation.amplifier", 32);
            amplifierField.setValue(String.valueOf(entry.amplifier));

            EditBox maxField = addField(left + EFFECT_WIDTH + NUMBER_WIDTH * 2 + GAP * 3, y, NUMBER_WIDTH, "config.renourisheddelight.starvation.max", 32);
            maxField.setValue(String.valueOf(entry.max));

            Button removeButton = addSmallButton("x", width / 2 + SIDE_MARGIN - BUTTON_HEIGHT, y, () -> removeStage(entry));
            rows.add(new StageRow(entry, effectField, afterField, amplifierField, maxField, removeButton));
        }
    }

    private boolean matchesSearch(StarvationEntry entry) {
        Holder<MobEffect> effect = StarvationEntry.resolveEffect(entry.effect);
        return matchesSearch(entry.effect != null ? entry.effect : "") || (effect != null && matchesSearch(effect.value().getDisplayName().getString()));
    }

    private void addStage() {
        if (!hasPendingStage()) return;
        config.starvation.add(pendingStage());
        scrollOffset = Integer.MAX_VALUE;
        newEffectField.setValue("");
        newAfterField.setValue("");
        newAmplifierField.setValue("");
        newMaxField.setValue("");
        rebuildContent();
    }

    private StarvationEntry pendingStage() {
        int after = parseInt(newAfterField.getValue(), 3600);
        int amplifier = Math.max(1, parseInt(newAmplifierField.getValue(), 1));
        int max = Math.max(amplifier, parseInt(newMaxField.getValue(), amplifier));
        return new StarvationEntry(newEffectField.getValue().trim(), after, amplifier, max);
    }

    private boolean hasPendingStage() {
        return !newEffectField.getValue().isBlank();
    }

    private void removeStage(StarvationEntry entry) {
        config.starvation.remove(entry);
        rebuildContent();
    }

    private void resetStages() {
        config.starvation.clear();
        config.starvation.addAll(StarvationEntry.defaults());
        scrollOffset = 0;
        rebuildContent();
    }

    private void applyRows() {
        for (StageRow row : rows) {
            row.entry().effect = row.effect().getValue().trim();
            row.entry().after = parseInt(row.after().getValue(), row.entry().after);
            row.entry().amplifier = Math.max(1, parseInt(row.amplifier().getValue(), row.entry().amplifier));
            row.entry().max = Math.max(row.entry().amplifier, parseInt(row.max().getValue(), row.entry().max));
        }
    }

    private @Nullable Component afterTooltip(String value) {
        try {
            return Component.literal(StringUtil.formatTickDuration(Integer.parseInt(value.trim()), 20));
        } catch (Exception exception) {
            return null;
        }
    }

    private void renderTooltips(int mouseX, int mouseY) {
        for (StageRow row : rows) {
            if (isHovering(row.after(), mouseX, mouseY)) {
                Component text = afterTooltip(row.after().getValue());
                if (text != null) setTooltipForNextRenderPass(text);
            }
        }
        if (!newAfterField.getValue().isEmpty() && isHovering(newAfterField, mouseX, mouseY)) {
            Component text = afterTooltip(newAfterField.getValue());
            if (text != null) setTooltipForNextRenderPass(text);
        }
    }

    private void applyValidationColors() {
        for (StageRow row : rows) {
            row.effect().setTextColor(StarvationEntry.resolveEffect(row.effect().getValue().trim()) != null ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
        }
        String pending = newEffectField.getValue().trim();
        newEffectField.setTextColor(pending.isEmpty() || StarvationEntry.resolveEffect(pending) != null ? NORMAL_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        applyValidationColors();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltips(mouseX, mouseY);
    }

    @Override
    protected void save() {
        applyRows();
        if (hasPendingStage()) config.starvation.add(pendingStage());
        holder.setFoodConfig(config);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = width / 2 - SIDE_MARGIN;
        drawHeaderLabel(graphics, "config.renourisheddelight.starvation.effect", left);
        drawHeaderLabel(graphics, "config.renourisheddelight.starvation.after", left + EFFECT_WIDTH + GAP);
        drawHeaderLabel(graphics, "config.renourisheddelight.starvation.amplifier", left + EFFECT_WIDTH + NUMBER_WIDTH + GAP * 2);
        drawHeaderLabel(graphics, "config.renourisheddelight.starvation.max", left + EFFECT_WIDTH + NUMBER_WIDTH * 2 + GAP * 3);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (config.starvation.isEmpty()) {
            drawCenteredMessage(graphics, "config.renourisheddelight.starvation.empty");
        } else if (rows.isEmpty()) {
            drawCenteredMessage(graphics, "config.renourisheddelight.starvation.no_results");
        }
    }

    private record StageRow(StarvationEntry entry, EditBox effect, EditBox after, EditBox amplifier, EditBox max, Button remove) {
        // do nothing
    }
}
