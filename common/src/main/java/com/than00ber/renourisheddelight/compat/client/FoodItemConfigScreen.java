package com.than00ber.renourisheddelight.compat.client;

import com.than00ber.renourisheddelight.config.CommonConfiguration;
import com.than00ber.renourisheddelight.config.data.FoodItemEntry;
import com.than00ber.renourisheddelight.config.data.WorldFoodConfig;
import com.than00ber.renourisheddelight.food.AttributeBonus;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import dev.architectury.platform.Platform;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

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

    private final @Nullable MinecraftServer server;
    private final List<FoodItemEntry> workingEntries;

    public FoodItemConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("config.renourisheddelight.food_items"));
        this.parent = parent;
        this.server = Minecraft.getInstance().getSingleplayerServer();
        this.workingEntries = server != null
                ? WorldFoodConfig.get(server).getEntries()
                : CommonConfiguration.getInstance().foodItemConfigurations;
    }

    private void saveWorkingEntries() {
        if (server != null) {
            WorldFoodConfig.get(server).setDirty();
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

        int buttonsY = height - 28;
        int buttonsWidth = 200;
        int buttonsLeft = centerX - buttonsWidth / 2;
        int gap = 5;
        int halfWidth = (buttonsWidth - gap) / 2;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(buttonsLeft, buttonsY, halfWidth, 20)
                .build());
        addRenderableWidget(createResetButton(buttonsLeft + halfWidth + gap, buttonsY, buttonsWidth - halfWidth - gap, 20, this::resetList));
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
        List<String> namespaces = computeNamespaces();

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
        int rowGap = ROW_HEIGHT - 17;
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

            Component nameLabel = item != null ? item.getDescription() : Component.literal(entry.item);
            Button nameButton = Button.builder(nameLabel, button -> openBonuses(entry))
                    .bounds(nameX, y, nameWidth, 20)
                    .tooltip(buildAttributesTooltip(entry))
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && modFilterButton != null && modFilterButton.isMouseOver(mouseX, mouseY)) {
            modFilterButton.playDownSound(Minecraft.getInstance().getSoundManager());
            cycleModFilterBackward();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void cycleModFilterBackward() {
        List<String> namespaces = computeNamespaces();
        int index = namespaces.indexOf(modFilter);
        int previous = index <= 0 ? namespaces.size() - 1 : index - 1;
        modFilter = namespaces.get(previous);
        scrollOffset = 0;
        rebuildContent();
    }

    private List<String> computeNamespaces() {
        List<String> namespaces = new ArrayList<>();
        namespaces.add(ALL_MODS);
        namespaces.addAll(new TreeSet<>(workingEntries.stream().map(this::namespaceOf).toList()));
        return namespaces;
    }

    private Tooltip buildAttributesTooltip(FoodItemEntry entry) {
        if (entry.attributes.isEmpty()) {
            return Tooltip.create(Component.translatable("config.renourisheddelight.food_items.no_bonuses"));
        }
        MutableComponent text = Component.empty();

        for (int i = 0; i < entry.attributes.size(); i++) {
            if (i > 0) text.append("\n");
            text.append(formatBonusLine(entry.attributes.get(i)));
        }
        return Tooltip.create(text);
    }

    private Component formatBonusLine(AttributeBonus bonus) {
        Holder<Attribute> attribute = ConsumableFoodInstance.resolveAttribute(bonus.attribute);
        Component name = attribute != null
                ? Component.translatable(attribute.value().getDescriptionId())
                : Component.literal(bonus.attribute);

        AttributeModifier.Operation operation = resolveOperation(bonus.operation);
        boolean percent = operation == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || operation == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
        double display = percent ? bonus.amount * 100.0 : bonus.amount;
        String durationText = StringUtil.formatTickDuration(bonus.duration, 20);

        Component amountLine = display >= 0
                ? Component.translatable("attribute.modifier.plus." + operation.id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(display), name)
                : Component.translatable("attribute.modifier.take." + operation.id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-display), name);

        return Component.empty().append(amountLine).append(" (" + durationText + ")");
    }

    private AttributeModifier.Operation resolveOperation(@Nullable String raw) {
        String value = raw != null ? raw.trim().toLowerCase(Locale.ROOT) : "";
        for (AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
            if (operation.getSerializedName().equals(value)) return operation;
        }
        return AttributeModifier.Operation.ADD_VALUE;
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
        FoodItemEntry existing = workingEntries.stream().filter(x -> id.equals(x.item)).findFirst().orElse(null);
        if (existing != null) return existing;

        FoodItemEntry entry = new FoodItemEntry(id, AttributeBonus.computeDefaultBonuses(item));
        workingEntries.add(entry);
        saveWorkingEntries();
        return entry;
    }

    private void removeItem(FoodItemEntry entry) {
        workingEntries.remove(entry);
        saveWorkingEntries();
        rebuildContent();
    }

    private void resetList() {
        workingEntries.clear();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.components().get(DataComponents.FOOD) != null) {
                String id = BuiltInRegistries.ITEM.getKey(item).toString();
                List<AttributeBonus> bonuses = AttributeBonus.computeDefaultBonuses(item);
                workingEntries.add(new FoodItemEntry(id, bonuses));
            }
        }
        saveWorkingEntries();
        scrollOffset = 0;
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
        MutableComponent scopeText = server != null
                ? Component.translatable("config.renourisheddelight.food_items.scope_world")
                : Component.translatable("config.renourisheddelight.food_items.scope_global");
        graphics.drawCenteredString(font, scopeText.withStyle(ChatFormatting.YELLOW), width / 2, 62, 0xFFFFFF);
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
