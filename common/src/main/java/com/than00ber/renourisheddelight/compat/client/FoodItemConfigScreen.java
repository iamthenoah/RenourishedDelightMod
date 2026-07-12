package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.ConfigUtil;
import com.than00ber.renourisheddelight.data.FoodItemEntry;
import com.than00ber.renourisheddelight.data.LevelFoodConfig;
import dev.architectury.platform.Platform;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public final class FoodItemConfigScreen extends AbstractFoodConfigScreen {

    private static final String ALL_MODS = "*";
    private static final int SIDE_MARGIN = 140;

    private final @Nullable Screen parent;
    private final List<IconEntry> icons = new ArrayList<>();
    private final List<AbstractWidget> rowWidgets = new ArrayList<>();

    private EditBox newItemField;
    private @Nullable CycleButton<String> modFilterButton;
    private String modFilter = ALL_MODS;
    private String searchQuery = "";
    private boolean noItemsConfigured;

    private final @Nullable Path levelConfigFile;
    private final List<FoodItemEntry> workingEntries;

    public FoodItemConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.food_items"));
        this.parent = parent;
        this.levelConfigFile = LevelFoodConfig.getInstance().resolveFile(Minecraft.getInstance().getSingleplayerServer());
        this.workingEntries = levelConfigFile != null
                ? LevelFoodConfig.getInstance().resolveEntries(levelConfigFile)
                : CommonConfiguration.getInstance().foodItemConfigurations;
    }

    private void saveWorkingEntries() {
        if (levelConfigFile != null) {
            LevelFoodConfig.getInstance().save(levelConfigFile, workingEntries);
        } else {
            AutoConfig.getConfigHolder(CommonConfiguration.class).save();
        }
    }

    @Override
    protected void init() {
        List<SuggestOption> itemOptions = buildItemOptions();
        int centerX = width / 2;
        int left = centerX - SIDE_MARGIN;

        EditBox searchField = new EditBox(font, left, 30, 170, 20, Component.translatable("config.renourisheddelight.food_items.search"));
        searchField.setMaxLength(256);
        searchField.setHint(Component.translatable("config.renourisheddelight.food_items.search_hint").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setResponder(value -> {
            searchQuery = value.toLowerCase(Locale.ROOT).trim();
            scrollOffset = 0;
            rebuildContent();
        });
        addRenderableWidget(searchField);

        newItemField = new EditBox(font, left, height - 56, 260, 20, Component.translatable("config.renourisheddelight.food_items.new_item"));
        newItemField.setMaxLength(256);
        newItemField.setHint(Component.literal("minecraft:bread").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(newItemField);
        suggestFields.add(new SuggestField(newItemField, itemOptions, true));

        addRenderableWidget(Button.builder(Component.literal("+"), button -> addItem())
                .bounds(centerX + 125, height - 56, 20, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(centerX - 100, height - 28, 200, 20)
                .build());
        rebuildContent();
    }

    @Override
    protected void rebuildContent() {
        for (AbstractWidget widget : rowWidgets) {
            removeWidget(widget);
        }
        rowWidgets.clear();
        icons.clear();

        if (modFilterButton != null) {
            removeWidget(modFilterButton);
        }

        int centerX = width / 2;
        List<FoodItemEntry> entries = workingEntries;
        noItemsConfigured = entries.isEmpty();
        List<String> namespaces = new ArrayList<>();
        namespaces.add(ALL_MODS);
        namespaces.addAll(new TreeSet<>(entries.stream().map(this::namespaceOf).toList()));

        if (!namespaces.contains(modFilter)) {
            modFilter = ALL_MODS;
        }
        modFilterButton = CycleButton.builder((String value) -> value.equals(ALL_MODS)
                        ? Component.translatable("config.renourisheddelight.food_items.all_mods")
                        : Component.literal(value).withStyle(isModInstalled(value) ? ChatFormatting.RESET : ChatFormatting.DARK_GRAY))
                .withValues(namespaces)
                .withInitialValue(modFilter)
                .withTooltip(value -> value.equals(ALL_MODS) || isModInstalled(value)
                        ? null
                        : Tooltip.create(Component.translatable("config.renourisheddelight.food_items.mod_not_installed").withStyle(ChatFormatting.RED)))
                .create(centerX + 35, 30, 110, 20, Component.translatable("config.renourisheddelight.food_items.filter"), (button, value) -> {
                    modFilter = value;
                    scrollOffset = 0;
                    rebuildContent();
                });
        addRenderableWidget(modFilterButton);
        rowWidgets.add(modFilterButton);

        List<FoodItemEntry> filtered = entries.stream()
                .filter(entry -> modFilter.equals(ALL_MODS) || namespaceOf(entry).equals(modFilter))
                .filter(entry -> searchQuery.isEmpty() || entry.item.toLowerCase(Locale.ROOT).contains(searchQuery))
                .toList();

        int listTop = 76;
        int listBottom = height - 68;
        int rowGap = ROW_HEIGHT - 20;
        int visibleRows = Math.max(1, (listBottom - listTop + rowGap) / ROW_HEIGHT);
        scrollMaxOffset = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, scrollMaxOffset);
        scrollVisibleRows = visibleRows;
        scrollTotalRows = Math.max(1, filtered.size());

        int iconX = centerX - SIDE_MARGIN;
        int nameX = iconX + 20;
        int nameWidth = 241;
        int removeX = centerX + 125;
        scrollTrackX = centerX + 150;
        scrollTrackTop = listTop;
        scrollTrackBottom = listBottom;

        for (int i = 0; i < visibleRows && i + scrollOffset < filtered.size(); i++) {
            FoodItemEntry entry = filtered.get(i + scrollOffset);
            int y = listTop + i * ROW_HEIGHT;

            Item item = resolveItem(entry.item);
            if (item != null) {
                icons.add(new IconEntry(new ItemStack(item), iconX, y + 2));
            }

            Button nameButton = Button.builder(Component.literal(entry.item), button -> openBonuses(entry))
                    .bounds(nameX, y, nameWidth, 20)
                    .build();
            addRenderableWidget(nameButton);
            rowWidgets.add(nameButton);

            Button removeButton = Button.builder(Component.literal("x"), button -> removeItem(entry))
                    .bounds(removeX, y, 20, 20)
                    .build();
            addRenderableWidget(removeButton);
            rowWidgets.add(removeButton);
        }
    }

    private String namespaceOf(FoodItemEntry entry) {
        int colon = entry.item.indexOf(':');
        return colon >= 0 ? entry.item.substring(0, colon) : "minecraft";
    }

    private boolean isModInstalled(String namespace) {
        return namespace.equals("minecraft") || Platform.isModLoaded(namespace);
    }

    private @Nullable Item resolveItem(String id) {
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
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            String name = item.getDescription().getString();
            String searchText = (id + " " + name).toLowerCase(Locale.ROOT);
            options.add(new SuggestOption(id, name, searchText));
        });
        options.sort(Comparator.comparing(SuggestOption::value, String.CASE_INSENSITIVE_ORDER));
        return options;
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
        FoodItemEntry entry = createEntry(item);
        newItemField.setValue("");
        openBonuses(entry);
    }

    private FoodItemEntry createEntry(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        FoodItemEntry existing = ConfigUtil.findEntry(workingEntries, id);
        if (existing != null) return existing;

        FoodItemEntry entry = ConfigUtil.seedEntry(workingEntries, item);
        saveWorkingEntries();
        return entry;
    }

    private void removeItem(FoodItemEntry entry) {
        workingEntries.remove(entry);
        saveWorkingEntries();
        rebuildContent();
    }

    private void openBonuses(FoodItemEntry entry) {
        minecraft.setScreen(new FoodItemBonusScreen(this, entry, this::saveWorkingEntries));
    }

    @Override
    protected void onDone() {
        saveWorkingEntries();
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Component hint = Component.translatable(levelConfigFile != null
                ? "config.renourisheddelight.food_items.scope_world"
                : "config.renourisheddelight.food_items.scope_global").withStyle(ChatFormatting.GRAY);
        graphics.drawCenteredString(font, hint, width / 2, 60, 0xAAAAAA);
    }

    @Override
    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (IconEntry icon : icons) {
            graphics.renderItem(icon.stack(), icon.x(), icon.y());
        }
        if (noItemsConfigured) {
            graphics.drawCenteredString(font, Component.translatable("config.renourisheddelight.food_items.empty"), width / 2, height / 2, 0xAAAAAA);
        }
    }

    private record IconEntry(ItemStack stack, int x, int y) {
        // do nothing
    }
}
