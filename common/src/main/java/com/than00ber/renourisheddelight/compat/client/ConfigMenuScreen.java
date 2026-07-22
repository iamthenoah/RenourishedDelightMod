package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.ClientConfiguration;
import com.than00ber.renourisheddelight.registry.EffectRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class ConfigMenuScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int FIELD_WIDTH = 210;
    private static final int RESET_GAP = 5;
    private static final int RESET_WIDTH = 45;
    private static final int TOTAL_WIDTH = FIELD_WIDTH + RESET_GAP + RESET_WIDTH;
    private static final int DONE_BUTTON_GAP = 10;
    private static final String DEFAULT_GOLDEN_PALETTE_ITEM = "minecraft:golden_carrot";
    private static final boolean DEFAULT_SHOW_FOOD_DISPLAY = false;
    private static final boolean DEFAULT_CLIP_ODD_MAX_HEALTH_HEART = true;

    private final @Nullable Screen parent;
    private EditBox goldenPaletteItemField;
    private boolean showFoodDisplayValue;
    private boolean clipOddMaxHealthHeartValue;
    private int top;

    public ConfigMenuScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ClientConfiguration config = ClientConfiguration.getInstance();
        showFoodDisplayValue = config.showFoodDisplayInInventory;
        clipOddMaxHealthHeartValue = config.clipOddMaxHealthHeart;

        int centerX = width / 2;
        int left = centerX - TOTAL_WIDTH / 2;
        top = height / 2 - (ROW_HEIGHT * 7) / 2;

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.client.hud_position"), button -> minecraft.setScreen(new HudPositionScreen(this)))
                .bounds(left, top, FIELD_WIDTH, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.reset"), button -> {
                    config.foodBarOffsetX = 0;
                    config.foodBarOffsetY = 0;
                    AutoConfig.getConfigHolder(ClientConfiguration.class).save();
                })
                .bounds(left + FIELD_WIDTH + RESET_GAP, top, RESET_WIDTH, 20)
                .build());

        goldenPaletteItemField = new EditBox(font, left, top + ROW_HEIGHT, FIELD_WIDTH, 20, Component.translatable("text.autoconfig.renourisheddelight/client.option.goldenPaletteItem"));
        goldenPaletteItemField.setMaxLength(256);
        goldenPaletteItemField.setValue(config.goldenPaletteItem.isBlank() ? DEFAULT_GOLDEN_PALETTE_ITEM : config.goldenPaletteItem);
        goldenPaletteItemField.setHint(Component.translatable("config.renourisheddelight.client.golden_palette_item_hint"));
        goldenPaletteItemField.setTooltip(Tooltip.create(Component.translatable("text.autoconfig.renourisheddelight/client.option.goldenPaletteItem.@Tooltip", EffectRegistry.NOURISHMENT.value().getDisplayName())));
        addRenderableWidget(goldenPaletteItemField);

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.reset"),
                        button -> goldenPaletteItemField.setValue(DEFAULT_GOLDEN_PALETTE_ITEM))
                .bounds(left + FIELD_WIDTH + RESET_GAP, top + ROW_HEIGHT, RESET_WIDTH, 20)
                .build());

        addToggle(left, top + ROW_HEIGHT * 2, "text.autoconfig.renourisheddelight/client.option.showFoodDisplayInInventory", showFoodDisplayValue, DEFAULT_SHOW_FOOD_DISPLAY, value -> showFoodDisplayValue = value);
        addToggle(left, top + ROW_HEIGHT * 3, "text.autoconfig.renourisheddelight/client.option.clipOddMaxHealthHeart", clipOddMaxHealthHeartValue, DEFAULT_CLIP_ODD_MAX_HEALTH_HEART, value -> clipOddMaxHealthHeartValue = value);

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.food_items"),
                        button -> minecraft.setScreen(new FoodItemConfigScreen(this)))
                .bounds(left, top + ROW_HEIGHT * 4, TOTAL_WIDTH, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.duration_multipliers"),
                        button -> minecraft.setScreen(new DurationMultiplierScreen(this)))
                .bounds(left, top + ROW_HEIGHT * 5, TOTAL_WIDTH, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        button -> onClose())
                .bounds(left, top + ROW_HEIGHT * 6 + DONE_BUTTON_GAP, TOTAL_WIDTH, 20)
                .build());
    }

    private void addToggle(int x, int y, String labelKey, boolean initial, boolean defaultValue, java.util.function.Consumer<Boolean> onChange) {
        boolean[] state = {initial};
        Button toggleButton = Button.builder(toggleLabel(labelKey, initial), button -> {
            state[0] = !state[0];
            onChange.accept(state[0]);
            button.setMessage(toggleLabel(labelKey, state[0]));
        }).bounds(x, y, FIELD_WIDTH, 20).build();
        addRenderableWidget(toggleButton);

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.reset"), button -> {
                    state[0] = defaultValue;
                    onChange.accept(defaultValue);
                    toggleButton.setMessage(toggleLabel(labelKey, defaultValue));
                })
                .bounds(x + FIELD_WIDTH + RESET_GAP, y, RESET_WIDTH, 20)
                .build());
    }

    private Component toggleLabel(String labelKey, boolean value) {
        Component valueText = Component.translatable(value ? "gui.yes" : "gui.no").withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
        return Component.translatable(labelKey).append(Component.literal(": ")).append(valueText);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, top - 18, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        ClientConfiguration config = ClientConfiguration.getInstance();
        config.goldenPaletteItem = goldenPaletteItemField.getValue().trim();
        config.showFoodDisplayInInventory = showFoodDisplayValue;
        config.clipOddMaxHealthHeart = clipOddMaxHealthHeartValue;
        AutoConfig.getConfigHolder(ClientConfiguration.class).save();
        minecraft.setScreen(parent);
    }
}
