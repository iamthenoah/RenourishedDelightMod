package com.than00ber.renourisheddelight.compat.patchouli;

import com.than00ber.renourisheddelight.config.data.FoodConfigHolder;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.food.DietRestrictions;
import com.than00ber.renourisheddelight.food.MealPlanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.List;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class OptimalMealComponent implements ICustomComponent {

    private static final int PAGE_WIDTH = 116;
    private static final int LIST_TOP = 24;
    private static final int ROW_HEIGHT = 17;
    private static final int MAX_ROWS = 7;

    private transient int x;
    private transient int y;
    private transient int slots;
    private transient boolean restricted;
    private transient MealPlanner.Meal meal = new MealPlanner.Meal(List.of());

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
        Diet diet = Minecraft.getInstance().player instanceof DietHolder holder ? holder.getDiet() : new Diet();
        slots = Math.max(1, diet.getCapacity());
        restricted = diet.isRestricted();

        if (Minecraft.getInstance().getConnection() instanceof FoodConfigHolder holder) {
            meal = MealPlanner.plan(diet, holder.getFoodConfig(), DietRestrictions.client());
        }
    }

    @Override
    public void render(GuiGraphics graphics, IComponentRenderContext context, float partialTicks, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        drawCentered(graphics, context, font, Component.translatable("book.renourisheddelight.optimal_meal.slots", slots), y, context.getHeaderColor());
        String key = restricted ? "book.renourisheddelight.optimal_meal.restricted" : "book.renourisheddelight.optimal_meal.unrestricted";
        drawCentered(graphics, context, font, Component.translatable(key), y + 10, context.getTextColor());

        if (meal.foods().isEmpty()) {
            drawCentered(graphics, context, font, Component.translatable("book.renourisheddelight.optimal_meal.empty"), y + LIST_TOP + 4, context.getTextColor());
            return;
        }
        int shown = Math.min(MAX_ROWS, meal.foods().size());

        for (int i = 0; i < shown; i++) {
            MealPlanner.Food food = meal.foods().get(i);
            int rowY = y + LIST_TOP + i * ROW_HEIGHT;
            context.renderItemStack(graphics, x, rowY, mouseX, mouseY, new ItemStack(food.item()));
            Component line = Component.translatable("book.renourisheddelight.optimal_meal.food", hearts(food.health()), StringUtil.formatTickDuration(food.duration()));
            graphics.drawString(font, line.copy().withStyle(context.getFont()), x + 20, rowY + 4, context.getTextColor(), false);
        }
        int footerY = y + LIST_TOP + shown * ROW_HEIGHT + 2;

        if (meal.foods().size() > shown) {
            drawCentered(graphics, context, font, Component.translatable("book.renourisheddelight.optimal_meal.more", meal.foods().size() - shown), footerY, context.getTextColor());
            footerY += 10;
        }
        drawCentered(graphics, context, font, Component.translatable("book.renourisheddelight.optimal_meal.total", hearts(meal.health())), footerY, context.getHeaderColor());
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        // no variables
    }

    // max health is in half hearts
    private static String hearts(double health) {
        return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(health / 2.0);
    }

    private void drawCentered(GuiGraphics graphics, IComponentRenderContext context, Font font, Component text, int textY, int color) {
        Component styled = text.copy().withStyle(context.getFont());
        graphics.drawString(font, styled, x + (PAGE_WIDTH - font.width(styled)) / 2, textY, color, false);
    }
}
