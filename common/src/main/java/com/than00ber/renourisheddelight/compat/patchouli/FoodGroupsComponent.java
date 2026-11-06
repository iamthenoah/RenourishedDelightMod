package com.than00ber.renourisheddelight.compat.patchouli;

import com.than00ber.renourisheddelight.food.DietRestrictions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.*;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class FoodGroupsComponent implements ICustomComponent {

    private static final int PAGE_WIDTH = 116;
    private static final int SLOT = 18;
    private static final int COLUMNS = PAGE_WIDTH / SLOT;
    private static final int ROWS = 6;
    private static final int GRID_TOP = 26;
    private static final int ARROW_WIDTH = 10;

    private static int view;

    private transient int x;
    private transient int y;
    private transient List<View> views = List.of();

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
        views = views();
        view = views.isEmpty() ? 0 : Math.floorMod(view, views.size());
    }

    @Override
    public void render(GuiGraphics graphics, IComponentRenderContext context, float partialTicks, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        if (views.isEmpty()) {
            drawCentered(graphics, context, font, Component.translatable("book.renourisheddelight.food_groups.empty"), y + 4);
            return;
        }
        View current = views.get(view);
        drawCentered(graphics, context, font, DietRestrictions.name(current.restriction()), y);
        drawCentered(graphics, context, font, Component.literal((view + 1) + " / " + views.size()), y + 11);
        graphics.drawString(font, Component.literal("<").withStyle(context.getFont()), x, y + 11, arrowColor(context, mouseX, mouseY, x), false);
        graphics.drawString(font, Component.literal(">").withStyle(context.getFont()), x + PAGE_WIDTH - ARROW_WIDTH + 4, y + 11, arrowColor(context, mouseX, mouseY, x + PAGE_WIDTH - ARROW_WIDTH), false);

        for (int i = 0; i < current.items().size(); i++) {
            int itemX = x + (i % COLUMNS) * SLOT + 1;
            int itemY = y + GRID_TOP + (i / COLUMNS) * SLOT;
            context.renderItemStack(graphics, itemX, itemY, mouseX, mouseY, new ItemStack(current.items().get(i)));
        }
    }

    @Override
    public boolean mouseClicked(IComponentRenderContext context, double mouseX, double mouseY, int mouseButton) {
        if (views.size() < 2) return false;

        if (context.isAreaHovered((int) mouseX, (int) mouseY, x, y + 9, ARROW_WIDTH, 12)) {
            view = Math.floorMod(view - 1, views.size());
            return true;
        }
        if (context.isAreaHovered((int) mouseX, (int) mouseY, x + PAGE_WIDTH - ARROW_WIDTH, y + 9, ARROW_WIDTH, 12)) {
            view = Math.floorMod(view + 1, views.size());
            return true;
        }
        return false;
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.Provider registries) {
        // no variables
    }

    private int arrowColor(IComponentRenderContext context, int mouseX, int mouseY, int arrowX) {
        return context.isAreaHovered(mouseX, mouseY, arrowX, y + 9, ARROW_WIDTH, 12) ? context.getHeaderColor() : context.getTextColor();
    }

    private void drawCentered(GuiGraphics graphics, IComponentRenderContext context, Font font, Component text, int textY) {
        Component styled = text.copy().withStyle(context.getFont());
        graphics.drawString(font, styled, x + (PAGE_WIDTH - font.width(styled)) / 2, textY, context.getHeaderColor(), false);
    }

    // one view per restriction, split further when a restriction holds more foods than fit on a page
    private static List<View> views() {
        Map<ResourceLocation, List<Item>> groups = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        DietRestrictions.client().all().forEach((item, restrictions) -> restrictions.forEach(restriction -> groups.computeIfAbsent(restriction, x -> new ArrayList<>()).add(item)));
        List<View> views = new ArrayList<>();
        int perView = COLUMNS * ROWS;

        groups.forEach((restriction, items) -> {
            items.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));

            for (int i = 0; i < items.size(); i += perView) {
                views.add(new View(restriction, items.subList(i, Math.min(items.size(), i + perView))));
            }
        });
        return views;
    }

    private record View(ResourceLocation restriction, List<Item> items) {
    }
}
