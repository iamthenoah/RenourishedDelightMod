package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class ClientSettingsScreen extends AbstractMenuScreen {

    private static final ClientConfiguration DEFAULTS = new ClientConfiguration();
    private static final int ROWS = 6;

    private boolean showFoodDisplayInInventoryValue;
    private boolean clipOddMaxHealthHeartValue;
    private boolean compactHealthBarValue;

    private EditBox goldenPaletteItemField;

    public ClientSettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.client"), parent);
    }

    @Override
    protected void init() {
        ClientConfiguration config = ClientConfiguration.getInstance();
        showFoodDisplayInInventoryValue = config.showFoodDisplayInInventory;
        clipOddMaxHealthHeartValue = config.clipOddMaxHealthHeart;
        compactHealthBarValue = config.compactHealthBar;

        layout(ROWS);
        int left = contentLeft();

        addHudPositionButton(left, top, config);
        goldenPaletteItemField = addGoldenPaletteItemField(left, top + ROW_HEIGHT, config.goldenPaletteItem.isBlank() ? DEFAULTS.goldenPaletteItem : config.goldenPaletteItem);
        addToggle(left, top + ROW_HEIGHT * 2, "text.autoconfig.renourisheddelight/client.option.showFoodDisplayInInventory", showFoodDisplayInInventoryValue, DEFAULTS.showFoodDisplayInInventory, value -> showFoodDisplayInInventoryValue = value);
        addToggle(left, top + ROW_HEIGHT * 3, "text.autoconfig.renourisheddelight/client.option.clipOddMaxHealthHeart", clipOddMaxHealthHeartValue, DEFAULTS.clipOddMaxHealthHeart, value -> clipOddMaxHealthHeartValue = value);
        addToggle(left, top + ROW_HEIGHT * 4, "text.autoconfig.renourisheddelight/client.option.compactHealthBar", compactHealthBarValue, DEFAULTS.compactHealthBar, value -> compactHealthBarValue = value);
        addDoneButton(left, top + ROW_HEIGHT * 5 + DONE_BUTTON_GAP);
    }

    private void addHudPositionButton(int x, int y, ClientConfiguration config) {
        Button hudPositionButton = Button.builder(Component.translatable("config.renourisheddelight.client.hud_position"), button -> minecraft.setScreen(new HudPositionScreen(this)))
                .bounds(x, y, FIELD_WIDTH, BUTTON_HEIGHT)
                .build();
        hudPositionButton.setTooltip(Tooltip.create(Component.translatable("config.renourisheddelight.client.hud_position.hint")));
        addRenderableWidget(hudPositionButton);
        addResetButton(x, y, FIELD_WIDTH, () -> {
            config.foodBarOffsetX = DEFAULTS.foodBarOffsetX;
            config.foodBarOffsetY = DEFAULTS.foodBarOffsetY;
        });
    }

    private EditBox addGoldenPaletteItemField(int x, int y, String initial) {
        String labelKey = "text.autoconfig.renourisheddelight/client.option.goldenPaletteItem";
        EditBox field = new EditBox(font, x, y, FIELD_WIDTH, BUTTON_HEIGHT, Component.translatable(labelKey));
        field.setMaxLength(256);
        field.setValue(initial);
        field.setHint(Component.translatable("config.renourisheddelight.client.golden_palette_item_hint"));
        field.setTooltip(Tooltip.create(Component.translatable(labelKey + ".@Tooltip", EffectRegistry.nourishment().value().getDisplayName())));
        addRenderableWidget(field);
        addResetButton(x, y, FIELD_WIDTH, () -> field.setValue(DEFAULTS.goldenPaletteItem));
        return field;
    }

    @Override
    protected void save() {
        ClientConfiguration config = ClientConfiguration.getInstance();
        config.goldenPaletteItem = goldenPaletteItemField.getValue().trim();
        config.showFoodDisplayInInventory = showFoodDisplayInInventoryValue;
        config.clipOddMaxHealthHeart = clipOddMaxHealthHeartValue;
        config.compactHealthBar = compactHealthBarValue;
        AutoConfig.getConfigHolder(ClientConfiguration.class).save();
    }
}
