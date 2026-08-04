package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.data.StarvationEntry;
import net.minecraft.ChatFormatting;
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
import java.util.Locale;

public final class StarvationScreen extends AbstractFoodConfigScreen {

    private static final int SIDE_MARGIN = 160;
    private static final int EFFECT_WIDTH = 160;
    private static final int NUMBER_WIDTH = 40;
    private static final int GAP = 5;
    private static final int HEADER_LABEL_Y = 76;
    private static final int LIST_TOP = 92;
    private static final int NORMAL_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xFF5555;

    private final @Nullable Screen parent;
    private final List<StarvationEntry> workingEntries;
    private final List<StageRow> rows = new ArrayList<>();
    private List<SuggestOption> effectOptions = List.of();
    private String searchQuery = "";

    private EditBox newEffectField;
    private EditBox newAfterField;
    private EditBox newAmplifierField;
    private EditBox newMaxField;

    public StarvationScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.starvation"));
        this.parent = parent;
        this.workingEntries = config.getStarvationConfig();
    }

    @Override
    protected void init() {
        effectOptions = buildEffectOptions();
        int centerX = width / 2;
        int left = centerX - SIDE_MARGIN;
        int newRowY = height - 56;

        modFilterField = new ModFilterField(() -> workingEntries.stream().map(entry -> entry.effect).toList(), namespace -> rebuildContent());

        EditBox searchField = new EditBox(font, left, 30, 170, 20, Component.translatable("config.renourisheddelight.starvation.search"));
        searchField.setMaxLength(256);
        searchField.setHint(Component.translatable("config.renourisheddelight.starvation.search_hint").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setResponder(value -> {
            searchQuery = value.toLowerCase(Locale.ROOT).trim();
            scrollOffset = 0;
            rebuildContent();
        });
        addViewWidget(searchField);

        newEffectField = createField(left, newRowY, EFFECT_WIDTH, "config.renourisheddelight.starvation.effect", 256);
        suggestFields.add(new SuggestField(newEffectField, effectOptions, true));
        newAfterField = createField(left + EFFECT_WIDTH + GAP, newRowY, NUMBER_WIDTH, "config.renourisheddelight.starvation.after", 32);
        newAmplifierField = createField(left + EFFECT_WIDTH + NUMBER_WIDTH + GAP * 2, newRowY, NUMBER_WIDTH, "config.renourisheddelight.starvation.amplifier", 32);
        newMaxField = createField(left + EFFECT_WIDTH + NUMBER_WIDTH * 2 + GAP * 3, newRowY, NUMBER_WIDTH, "config.renourisheddelight.starvation.max", 32);

        addRenderableWidget(Button.builder(Component.literal("+"), button -> addStage())
                .bounds(centerX + SIDE_MARGIN - 20, newRowY, 20, 20)
                .build());

        int buttonsY = height - 28;
        int buttonsWidth = 200;
        int buttonsLeft = centerX - buttonsWidth / 2;
        int halfWidth = (buttonsWidth - GAP) / 2;

        addViewWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(buttonsLeft, buttonsY, halfWidth, 20)
                .build());
        addRenderableWidget(createResetButton(buttonsLeft + halfWidth + GAP, buttonsY, buttonsWidth - halfWidth - GAP, 20, this::resetStages));
        rebuildContent();
    }

    private EditBox createField(int x, int y, int width, String key, int maxLength) {
        EditBox box = new EditBox(font, x, y, width, 20, Component.translatable(key));
        box.setMaxLength(maxLength);
        box.setHint(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(box);
        return box;
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

        int centerX = width / 2;
        int left = centerX - SIDE_MARGIN;
        int listBottom = height - 68;
        int rowGap = ROW_HEIGHT - 20;
        int visibleRows = Math.max(1, (listBottom - LIST_TOP + rowGap) / ROW_HEIGHT);

        modFilterField.rebuild(centerX + 35, 30, 110, 20, Component.translatable("config.renourisheddelight.filter"));

        List<StarvationEntry> filtered = workingEntries.stream()
                .filter(entry -> modFilterField.matches(entry.effect != null ? entry.effect : ""))
                .filter(this::matchesSearch)
                .sorted(Comparator.comparingInt(x -> x.after))
                .toList();
        scrollMaxOffset = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, filtered.size());

        scrollTrackX = centerX + SIDE_MARGIN + 10;
        scrollTrackTop = LIST_TOP;
        scrollTrackBottom = listBottom;

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            StarvationEntry entry = filtered.get(i + scrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;

            EditBox effectField = createField(left, y, EFFECT_WIDTH, "config.renourisheddelight.starvation.effect", 256);
            effectField.setValue(entry.effect != null ? entry.effect : "");
            suggestFields.add(new SuggestField(effectField, effectOptions));

            EditBox afterField = createField(left + EFFECT_WIDTH + GAP, y, NUMBER_WIDTH, "config.renourisheddelight.starvation.after", 32);
            afterField.setValue(String.valueOf(entry.after));

            EditBox amplifierField = createField(left + EFFECT_WIDTH + NUMBER_WIDTH + GAP * 2, y, NUMBER_WIDTH, "config.renourisheddelight.starvation.amplifier", 32);
            amplifierField.setValue(String.valueOf(entry.amplifier));

            EditBox maxField = createField(left + EFFECT_WIDTH + NUMBER_WIDTH * 2 + GAP * 3, y, NUMBER_WIDTH, "config.renourisheddelight.starvation.max", 32);
            maxField.setValue(String.valueOf(entry.max));

            Button removeButton = Button.builder(Component.literal("x"), button -> removeStage(entry))
                    .bounds(centerX + SIDE_MARGIN - 20, y, 20, 20)
                    .build();
            addRenderableWidget(removeButton);

            rows.add(new StageRow(entry, effectField, afterField, amplifierField, maxField, removeButton));
        }
    }

    private boolean matchesSearch(StarvationEntry entry) {
        if (searchQuery.isEmpty()) return true;
        String id = entry.effect != null ? entry.effect.toLowerCase(Locale.ROOT) : "";
        if (id.contains(searchQuery)) return true;

        Holder<MobEffect> effect = StarvationEntry.resolveEffect(entry.effect);
        if (effect == null) return false;

        return effect.value().getDisplayName().getString().toLowerCase(Locale.ROOT).contains(searchQuery);
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

    private void addStage() {
        if (!editable) return;
        String effect = newEffectField.getValue().trim();
        if (effect.isEmpty()) return;

        int after = parseInt(newAfterField.getValue(), 3600);
        int amplifier = Math.max(1, parseInt(newAmplifierField.getValue(), 1));
        int max = Math.max(amplifier, parseInt(newMaxField.getValue(), amplifier));
        workingEntries.add(new StarvationEntry(effect, after, amplifier, max));
        scrollOffset = Integer.MAX_VALUE;

        newEffectField.setValue("");
        newAfterField.setValue("");
        newAmplifierField.setValue("");
        newMaxField.setValue("");

        save();
        rebuildContent();
    }

    private void removeStage(StarvationEntry entry) {
        if (!editable) return;
        workingEntries.remove(entry);
        save();
        rebuildContent();
    }

    private void resetStages() {
        if (!editable) return;
        workingEntries.clear();
        workingEntries.add(new StarvationEntry("minecraft:slowness", 3600, 1, 3));
        workingEntries.add(new StarvationEntry("minecraft:mining_fatigue", 7200, 1, 3));
        workingEntries.add(new StarvationEntry("minecraft:weakness", 10800, 1, 2));
        save();
        scrollOffset = 0;
        rebuildContent();
    }

    private void applyRows() {
        if (!editable) return;

        for (StageRow row : rows) {
            row.entry().effect = row.effect().getValue().trim();
            row.entry().after = parseInt(row.after().getValue(), row.entry().after);
            row.entry().amplifier = Math.max(1, parseInt(row.amplifier().getValue(), row.entry().amplifier));
            row.entry().max = Math.max(row.entry().amplifier, parseInt(row.max().getValue(), row.entry().max));
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private @Nullable Component afterTooltip(String value) {
        try {
            return Component.literal(StringUtil.formatTickDuration(Integer.parseInt(value.trim()), 20));
        } catch (Exception exception) {
            return null;
        }
    }

    private boolean isHovering(EditBox box, int mouseX, int mouseY) {
        return mouseX >= box.getX() && mouseX < box.getX() + box.getWidth()
                && mouseY >= box.getY() && mouseY < box.getY() + box.getHeight();
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
    protected void onDone() {
        applyRows();
        save();
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = width / 2 - SIDE_MARGIN;
        graphics.drawString(font, Component.translatable("config.renourisheddelight.starvation.effect"), left, HEADER_LABEL_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.starvation.after"), left + EFFECT_WIDTH + GAP, HEADER_LABEL_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.starvation.amplifier"), left + EFFECT_WIDTH + NUMBER_WIDTH + GAP * 2, HEADER_LABEL_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("config.renourisheddelight.starvation.max"), left + EFFECT_WIDTH + NUMBER_WIDTH * 2 + GAP * 3, HEADER_LABEL_Y, 0xFFFFFF);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (workingEntries.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.starvation.empty"), width / 2, height / 2, 0xAAAAAA);
        } else if (rows.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.starvation.no_results"), width / 2, height / 2, 0xAAAAAA);
        }
    }

    private record StageRow(StarvationEntry entry, EditBox effect, EditBox after, EditBox amplifier, EditBox max, Button remove) {
    }
}
