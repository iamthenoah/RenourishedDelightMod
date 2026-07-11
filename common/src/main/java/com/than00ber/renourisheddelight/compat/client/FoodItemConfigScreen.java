package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.Configuration;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class FoodItemConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final String ALL_MODS = "*";
    private static final int SCROLLBAR_WIDTH = 6;

    private final @Nullable Screen parent;
    private final List<IconEntry> icons = new ArrayList<>();

    private EditBox newItemField;
    private int scrollOffset = 0;
    private String modFilter = ALL_MODS;

    private int scrollTrackX;
    private int scrollTrackTop;
    private int scrollTrackBottom;
    private int scrollMaxOffset;
    private int scrollVisibleRows;
    private int scrollTotalRows;
    private boolean draggingScrollbar;

    public FoodItemConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.food_items"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        icons.clear();
        int centerX = width / 2;

        newItemField = new EditBox(font, centerX - 150, 30, 220, 20, Component.translatable("config.renourisheddelight.food_items.new_item"));
        newItemField.setMaxLength(256);
        newItemField.setHint(Component.literal("minecraft:bread"));
        addRenderableWidget(newItemField);

        addRenderableWidget(Button.builder(Component.translatable("config.renourisheddelight.food_items.add"), button -> addItem())
                .bounds(centerX + 75, 30, 75, 20)
                .build());

        List<Configuration.FoodItemEntry> entries = Configuration.Common.getInstance().foodItemConfigurations;
        List<String> namespaces = new ArrayList<>();
        namespaces.add(ALL_MODS);
        namespaces.addAll(new TreeSet<>(entries.stream().map(this::namespaceOf).toList()));

        if (!namespaces.contains(modFilter)) {
            modFilter = ALL_MODS;
        }
        addRenderableWidget(CycleButton.builder((String value) -> value.equals(ALL_MODS)
                        ? Component.translatable("config.renourisheddelight.food_items.all_mods")
                        : Component.literal(value))
                .withValues(namespaces)
                .withInitialValue(modFilter)
                .create(centerX - 100, 54, 200, 20, Component.translatable("config.renourisheddelight.food_items.filter"), (button, value) -> {
                    modFilter = value;
                    scrollOffset = 0;
                    rebuild();
                }));

        List<Configuration.FoodItemEntry> filtered = modFilter.equals(ALL_MODS)
                ? entries
                : entries.stream().filter(entry -> namespaceOf(entry).equals(modFilter)).toList();

        int listTop = 82;
        int listBottom = height - 36;
        int rowGap = ROW_HEIGHT - 20;
        int visibleRows = Math.max(1, (listBottom - listTop + rowGap) / ROW_HEIGHT);
        scrollMaxOffset = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, filtered.size());

        int iconX = centerX - 150;
        int nameX = iconX + 20;
        int nameWidth = 210;
        int removeX = centerX + 90;
        scrollTrackX = centerX + 130;
        scrollTrackTop = listTop;
        scrollTrackBottom = listTop + visibleRows * ROW_HEIGHT - rowGap;

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            Configuration.FoodItemEntry entry = filtered.get(i + scrollOffset);
            int y = listTop + i * ROW_HEIGHT;

            Item item = resolveItem(entry.item);
            if (item != null) {
                icons.add(new IconEntry(new ItemStack(item), iconX, y + 2));
            }

            addRenderableWidget(Button.builder(Component.literal(entry.item), button -> openBonuses(entry))
                    .bounds(nameX, y, nameWidth, 20)
                    .build());

            addRenderableWidget(Button.builder(Component.literal("X"), button -> removeItem(entry))
                    .bounds(removeX, y, 35, 20)
                    .build());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(centerX - 100, height - 28, 200, 20)
                .build());
    }

    private String namespaceOf(Configuration.FoodItemEntry entry) {
        int colon = entry.item.indexOf(':');
        return colon >= 0 ? entry.item.substring(0, colon) : "minecraft";
    }

    private @Nullable Item resolveItem(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            return item != Items.AIR ? item : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private void addItem() {
        String value = newItemField.getValue().trim();
        if (value.isEmpty()) return;
        ResourceLocation id;

        try {
            id = ResourceLocation.parse(value);
        } catch (Exception exception) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) return;
        String key = id.toString();
        List<Configuration.FoodItemEntry> entries = Configuration.Common.getInstance().foodItemConfigurations;

        for (Configuration.FoodItemEntry entry : entries) {
            if (entry.item.equals(key)) {
                newItemField.setValue("");
                openBonuses(entry);
                return;
            }
        }
        Configuration.FoodItemEntry entry = new Configuration.FoodItemEntry();
        entry.item = key;
        entries.add(entry);
        newItemField.setValue("");
        AutoConfig.getConfigHolder(Configuration.Common.class).save();
        rebuild();
    }

    private void removeItem(Configuration.FoodItemEntry entry) {
        Configuration.Common.getInstance().foodItemConfigurations.remove(entry);
        AutoConfig.getConfigHolder(Configuration.Common.class).save();
        rebuild();
    }

    private void openBonuses(Configuration.FoodItemEntry entry) {
        minecraft.setScreen(new FoodItemBonusScreen(this, entry));
    }

    private void onDone() {
        AutoConfig.getConfigHolder(Configuration.Common.class).save();
        minecraft.setScreen(parent);
    }

    private boolean isInsideScrollbar(double mouseX, double mouseY) {
        return scrollMaxOffset > 0
                && mouseX >= scrollTrackX && mouseX <= scrollTrackX + SCROLLBAR_WIDTH
                && mouseY >= scrollTrackTop && mouseY <= scrollTrackBottom;
    }

    private void scrollToMouse(double mouseY) {
        double ratio = (mouseY - scrollTrackTop) / Math.max(1, scrollTrackBottom - scrollTrackTop);
        scrollOffset = (int) Math.round(ratio * scrollMaxOffset);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > scrollMaxOffset) scrollOffset = scrollMaxOffset;
        rebuild();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            scrollToMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            scrollToMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= (int) Math.signum(scrollY);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > scrollMaxOffset) scrollOffset = scrollMaxOffset;
        rebuild();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        for (IconEntry icon : icons) {
            graphics.renderItem(icon.stack(), icon.x(), icon.y());
        }
        if (scrollMaxOffset > 0) {
            int trackHeight = scrollTrackBottom - scrollTrackTop;
            int thumbHeight = Math.max(12, trackHeight * scrollVisibleRows / scrollTotalRows);
            int thumbTravel = trackHeight - thumbHeight;
            int thumbY = scrollTrackTop + thumbTravel * scrollOffset / scrollMaxOffset;

            graphics.fill(scrollTrackX, scrollTrackTop, scrollTrackX + SCROLLBAR_WIDTH, scrollTrackBottom, 0x40000000);
            graphics.fill(scrollTrackX, thumbY, scrollTrackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public void onClose() {
        onDone();
    }

    private record IconEntry(ItemStack stack, int x, int y) {
    }
}
