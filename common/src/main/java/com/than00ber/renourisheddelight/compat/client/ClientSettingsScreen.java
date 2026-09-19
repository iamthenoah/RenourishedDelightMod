package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class ClientSettingsScreen extends AbstractMenuScreen {

    private static final ClientConfiguration DEFAULTS = new ClientConfiguration();
    private static final int FIELD_WIDTH = 210;
    private static final int RESET_GAP = 5;
    private static final int RESET_WIDTH = 45;
    private static final int TOTAL_WIDTH = FIELD_WIDTH + RESET_GAP + RESET_WIDTH;
    private static final int ROWS = 6;

    private EditBox goldenPaletteItemField;
    private boolean showFoodDisplayValue;
    private boolean clipOddMaxHealthHeartValue;
    private boolean compactHealthBarValue;

    public ClientSettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.client"), parent);
    }

    @Override
    protected int contentWidth() {
        return TOTAL_WIDTH;
    }

    @Override
    protected void init() {
        ClientConfiguration config = ClientConfiguration.getInstance();
        showFoodDisplayValue = config.showFoodDisplayInInventory;
        clipOddMaxHealthHeartValue = config.clipOddMaxHealthHeart;
        compactHealthBarValue = config.compactHealthBar;

        layout(ROWS);
        int left = contentLeft();

        Button hudPositionButton = Button.builder(Component.translatable("config.renourisheddelight.client.hud_position"), button -> minecraft.setScreen(new HudPositionScreen(this)))
                .bounds(left, top, FIELD_WIDTH, BUTTON_HEIGHT)
                .build();
        hudPositionButton.setTooltip(Tooltip.create(Component.translatable("config.renourisheddelight.client.hud_position.hint")));
        addRenderableWidget(hudPositionButton);
        addResetButton(left, top, () -> {
            config.foodBarOffsetX = DEFAULTS.foodBarOffsetX;
            config.foodBarOffsetY = DEFAULTS.foodBarOffsetY;
            AutoConfig.getConfigHolder(ClientConfiguration.class).save();
        });

        goldenPaletteItemField = new EditBox(font, left, top + ROW_HEIGHT, FIELD_WIDTH, BUTTON_HEIGHT, Component.translatable("text.autoconfig.renourisheddelight/client.option.goldenPaletteItem"));
        goldenPaletteItemField.setMaxLength(256);
        goldenPaletteItemField.setValue(config.goldenPaletteItem.isBlank() ? DEFAULTS.goldenPaletteItem : config.goldenPaletteItem);
        goldenPaletteItemField.setHint(Component.translatable("config.renourisheddelight.client.golden_palette_item_hint"));
        goldenPaletteItemField.setTooltip(Tooltip.create(Component.translatable("text.autoconfig.renourisheddelight/client.option.goldenPaletteItem.@Tooltip", EffectRegistry.nourishment().value().getDisplayName())));
        addRenderableWidget(goldenPaletteItemField);
        addResetButton(left, top + ROW_HEIGHT, () -> goldenPaletteItemField.setValue(DEFAULTS.goldenPaletteItem));

        addToggle(left, top + ROW_HEIGHT * 2, "text.autoconfig.renourisheddelight/client.option.showFoodDisplayInInventory", showFoodDisplayValue, DEFAULTS.showFoodDisplayInInventory, value -> showFoodDisplayValue = value);
        addToggle(left, top + ROW_HEIGHT * 3, "text.autoconfig.renourisheddelight/client.option.clipOddMaxHealthHeart", clipOddMaxHealthHeartValue, DEFAULTS.clipOddMaxHealthHeart, value -> clipOddMaxHealthHeartValue = value);
        addToggle(left, top + ROW_HEIGHT * 4, "text.autoconfig.renourisheddelight/client.option.compactHealthBar", compactHealthBarValue, DEFAULTS.compactHealthBar, value -> compactHealthBarValue = value);

        addDoneButton(left, top + ROW_HEIGHT * (ROWS - 1) + DONE_BUTTON_GAP);
    }

    private void addResetButton(int x, int y, Runnable action) {
        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.reset"), button -> action.run())
                .bounds(x + FIELD_WIDTH + RESET_GAP, y, RESET_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private void addToggle(int x, int y, String labelKey, boolean initial, boolean defaultValue, Consumer<Boolean> onChange) {
        boolean[] state = {initial};
        Button toggleButton = Button.builder(toggleLabel(labelKey, initial), button -> {
            state[0] = !state[0];
            onChange.accept(state[0]);
            button.setMessage(toggleLabel(labelKey, state[0]));
        }).bounds(x, y, FIELD_WIDTH, BUTTON_HEIGHT).build();
        toggleButton.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip")));
        addRenderableWidget(toggleButton);

        addResetButton(x, y, () -> {
            state[0] = defaultValue;
            onChange.accept(defaultValue);
            toggleButton.setMessage(toggleLabel(labelKey, defaultValue));
        });
    }

    private Component toggleLabel(String labelKey, boolean value) {
        Component valueText = Component.translatable(value ? "gui.yes" : "gui.no").withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
        return Component.translatable(labelKey).append(Component.literal(": ")).append(valueText);
    }

    @Override
    protected void save() {
        ClientConfiguration config = ClientConfiguration.getInstance();
        config.goldenPaletteItem = goldenPaletteItemField.getValue().trim();
        config.showFoodDisplayInInventory = showFoodDisplayValue;
        config.clipOddMaxHealthHeart = clipOddMaxHealthHeartValue;
        config.compactHealthBar = compactHealthBarValue;
        AutoConfig.getConfigHolder(ClientConfiguration.class).save();
    }
}
