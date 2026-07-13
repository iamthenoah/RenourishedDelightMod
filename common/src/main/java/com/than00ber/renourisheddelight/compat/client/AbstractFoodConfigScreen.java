package com.than00ber.renourisheddelight.compat.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class AbstractFoodConfigScreen extends Screen {

    protected static final int ROW_HEIGHT = 24;
    protected static final int SCROLLBAR_WIDTH = 6;
    protected static final int SUGGESTION_ROW_HEIGHT = 14;
    protected static final int MAX_SUGGESTIONS = 40;
    protected static final int VISIBLE_SUGGESTIONS = 10;
    protected static final float SUGGESTION_Z = 400.0F;
    protected static final int TITLE_Y = 11;

    protected final List<SuggestField> suggestFields = new ArrayList<>();

    protected int scrollOffset = 0;
    protected int scrollTrackX;
    protected int scrollTrackTop;
    protected int scrollTrackBottom;
    protected int scrollMaxOffset;
    protected int scrollVisibleRows;
    protected int scrollTotalRows;
    protected boolean draggingScrollbar;

    protected AbstractFoodConfigScreen(Component title) {
        super(title);
    }

    protected abstract void rebuildContent();

    protected abstract void onDone();

    protected void renderHeaderActions(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    protected void renderScrollableContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    protected Button createResetButton(int x, int y, int width, int height, Runnable action) {
        boolean[] armed = {false};
        return Button.builder(Component.translatable("config.renourisheddelight.food_items.reset"), button -> {
            if (armed[0]) {
                armed[0] = false;
                button.setMessage(Component.translatable("config.renourisheddelight.food_items.reset"));
                action.run();
            } else {
                armed[0] = true;
                button.setMessage(Component.translatable("config.renourisheddelight.food_items.reset_confirm").withStyle(ChatFormatting.YELLOW));
            }
        }).bounds(x, y, width, height).build();
    }

    protected boolean isInsideScrollbar(double mouseX, double mouseY) {
        return scrollMaxOffset > 0
                && mouseX >= scrollTrackX && mouseX <= scrollTrackX + SCROLLBAR_WIDTH
                && mouseY >= scrollTrackTop && mouseY <= scrollTrackBottom;
    }

    protected void scrollToMouse(double mouseY) {
        double ratio = (mouseY - scrollTrackTop) / Math.max(1, scrollTrackBottom - scrollTrackTop);
        scrollOffset = Math.clamp((int) Math.round(ratio * scrollMaxOffset), 0, scrollMaxOffset);
        rebuildContent();
    }

    protected @Nullable SuggestField openSuggestFieldAt(double mouseX, double mouseY) {
        for (SuggestField field : suggestFields) {
            if (field.box.isFocused() && !field.matches.isEmpty() && field.isMouseOver(mouseX, mouseY)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (SuggestField field : suggestFields) {
            if (field.box.isFocused() && !field.matches.isEmpty()) {
                int index = field.indexAt(mouseX, mouseY);
                if (index >= 0) {
                    field.box.setValue(field.matches.get(index).value());
                    field.matches = List.of();
                    return true;
                }
            }
        }
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
        SuggestField hovered = openSuggestFieldAt(mouseX, mouseY);
        if (hovered != null) {
            hovered.scroll(-(int) Math.signum(scrollY));
            return true;
        }
        if (scrollMaxOffset > 0) {
            scrollOffset = Math.clamp(scrollOffset - (int) Math.signum(scrollY), 0, scrollMaxOffset);
            rebuildContent();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(0, 54 + 1, width, height - 60, 0x40000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPanelChrome(graphics);
        graphics.drawCenteredString(font, title, width / 2, TITLE_Y, 0xFFFFFF);

        renderHeaderActions(graphics, mouseX, mouseY, partialTick);
        renderScrollableContent(graphics, mouseX, mouseY, partialTick);

        if (scrollMaxOffset > 0) {
            int trackHeight = scrollTrackBottom - scrollTrackTop;
            double visibleFraction = Math.min(1.0, scrollVisibleRows / (double) scrollTotalRows);
            int thumbHeight = Math.max(12, (int) Math.round(trackHeight * visibleFraction));
            int thumbTravel = trackHeight - thumbHeight;
            double scrollFraction = scrollOffset / (double) scrollMaxOffset;
            int thumbY = scrollTrackTop + (int) Math.round(thumbTravel * scrollFraction);

            graphics.fill(scrollTrackX, scrollTrackTop, scrollTrackX + SCROLLBAR_WIDTH, scrollTrackBottom, 0x40000000);
            graphics.fill(scrollTrackX, thumbY, scrollTrackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
        }

        for (SuggestField field : suggestFields) {
            if (field.box.isFocused() && !field.matches.isEmpty()) {
                field.render(graphics, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        onDone();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    protected void renderPanelChrome(GuiGraphics graphics) {
        int headerBottom = 54;
        int footerTop = height - 62;

        graphics.fill(0, headerBottom, width, headerBottom + 1, 0x20FFFFFF);
        graphics.fill(0, headerBottom + 1, width, headerBottom + 2, 0x80000000);

        graphics.fill(0, footerTop, width, footerTop + 1, 0x80000000);
        graphics.fill(0, footerTop + 1, width, footerTop + 2, 0x20FFFFFF);
    }

    protected record SuggestOption(String value, String name, String searchText) {
        protected boolean hasDistinctName() {
            return !name.equals(value);
        }

        protected String plainText() {
            return hasDistinctName() ? name + " (" + value + ")" : name;
        }
    }

    protected final class SuggestField {
        protected final EditBox box;
        private final List<SuggestOption> pool;
        private final boolean dropUp;
        protected List<SuggestOption> matches = List.of();
        private int scrollOffset = 0;

        protected SuggestField(EditBox box, List<SuggestOption> pool) {
            this(box, pool, false);
        }

        protected SuggestField(EditBox box, List<SuggestOption> pool, boolean dropUp) {
            this.box = box;
            this.pool = pool;
            this.dropUp = dropUp;
            box.setResponder(value -> updateMatches());
            updateMatches();
        }

        private void updateMatches() {
            String query = box.getValue().trim().toLowerCase(Locale.ROOT);
            matches = pool.stream()
                    .filter(option -> query.isEmpty() || option.searchText().contains(query))
                    .sorted(Comparator
                            .<SuggestOption>comparingInt(option -> option.value().toLowerCase(Locale.ROOT).startsWith(query) ? 0 : 1)
                            .thenComparing(SuggestOption::value, String.CASE_INSENSITIVE_ORDER))
                    .limit(MAX_SUGGESTIONS)
                    .toList();
            scrollOffset = 0;
        }

        protected int visibleCount() {
            return Math.min(VISIBLE_SUGGESTIONS, matches.size());
        }

        protected void scroll(int direction) {
            int maxOffset = Math.max(0, matches.size() - visibleCount());
            scrollOffset = Math.clamp(scrollOffset + direction, 0, maxOffset);
        }

        private int contentWidth() {
            int widest = box.getWidth();
            int end = Math.min(matches.size(), scrollOffset + visibleCount());
            for (int i = scrollOffset; i < end; i++) {
                widest = Math.max(widest, font.width(matches.get(i).plainText()) + 4);
            }
            if (matches.size() > visibleCount()) widest += 6;
            return widest;
        }

        private int listTop() {
            return dropUp
                    ? box.getY() - visibleCount() * SUGGESTION_ROW_HEIGHT
                    : box.getY() + box.getHeight();
        }

        protected boolean isMouseOver(double mouseX, double mouseY) {
            int x = box.getX();
            int y = listTop();
            int listWidth = contentWidth();
            return mouseX >= x && mouseX <= x + listWidth
                    && mouseY >= y && mouseY <= y + visibleCount() * SUGGESTION_ROW_HEIGHT;
        }

        protected int indexAt(double mouseX, double mouseY) {
            if (!isMouseOver(mouseX, mouseY)) return -1;
            int y = listTop();
            int localIndex = (int) ((mouseY - y) / SUGGESTION_ROW_HEIGHT);
            int globalIndex = scrollOffset + localIndex;
            return globalIndex < matches.size() ? globalIndex : -1;
        }

        protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
            int x = box.getX();
            int y = listTop();
            int listWidth = contentWidth();
            int visible = visibleCount();
            int totalHeight = visible * SUGGESTION_ROW_HEIGHT;
            boolean scrollable = matches.size() > visible;
            int barWidth = 2;
            int textX = scrollable ? x + barWidth + 4 : x + 2;

            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, SUGGESTION_Z);
            graphics.fill(x, y, x + listWidth, y + totalHeight, 0xE0000000);
            for (int i = 0; i < visible; i++) {
                SuggestOption option = matches.get(scrollOffset + i);
                int rowY = y + i * SUGGESTION_ROW_HEIGHT;
                boolean hovered = mouseX >= x && mouseX <= x + listWidth && mouseY >= rowY && mouseY <= rowY + SUGGESTION_ROW_HEIGHT;

                if (hovered) {
                    graphics.drawString(font, option.plainText(), textX, rowY + 3, 0xFFFF00, false);
                } else if (option.hasDistinctName()) {
                    Component styled = Component.literal(option.name()).withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(" (" + option.value() + ")").withStyle(ChatFormatting.GRAY));
                    graphics.drawString(font, styled, textX, rowY + 3, 0xFFFFFF, false);
                } else {
                    graphics.drawString(font, option.name(), textX, rowY + 3, 0xFFFFFF, false);
                }
            }

            if (scrollable) {
                graphics.fill(x, y, x + barWidth, y + totalHeight, 0x40FFFFFF);

                double fraction = visible / (double) matches.size();
                int thumbHeight = Math.max(6, (int) Math.round(totalHeight * fraction));
                int thumbTravel = totalHeight - thumbHeight;
                double scrollFraction = scrollOffset / (double) Math.max(1, matches.size() - visible);
                int thumbY = y + (int) Math.round(thumbTravel * scrollFraction);
                graphics.fill(x, thumbY, x + barWidth, thumbY + thumbHeight, 0xFFFFFFFF);
            }
            graphics.pose().popPose();
        }
    }
}
