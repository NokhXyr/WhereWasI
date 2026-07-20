package dev.nokhxyr.wherewasi.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;

/**
 * A minimal, dependency-free scrolling list: a viewport, a list of variable-height
 * rows, a clamped scroll offset, wheel support and a thin scrollbar. Used by the
 * timeline and zones screens. Rows draw themselves via {@link Row}.
 */
public final class ScrollList {

    public interface Row {
        int height();

        void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered);

        /** @return true if the click was consumed. {@code y} is the row's on-screen top. */
        default boolean click(double mouseX, double mouseY, int x, int y, int width) {
            return false;
        }
    }

    private int x, y, width, height;
    private final List<Row> rows = new ArrayList<>();
    private double scroll;

    public void bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        clamp();
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        clamp();
    }

    private int contentHeight() {
        int total = 0;
        for (Row r : rows) {
            total += r.height();
        }
        return total;
    }

    private void clamp() {
        double max = Math.max(0, contentHeight() - height);
        scroll = Math.max(0, Math.min(scroll, max));
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        g.enableScissor(x, y, x + width, y + height);
        int cy = y - (int) scroll;
        for (Row r : rows) {
            int rh = r.height();
            if (cy + rh >= y && cy <= y + height) {
                boolean hovered = mouseX >= x && mouseX <= x + width
                        && mouseY >= Math.max(cy, y) && mouseY < Math.min(cy + rh, y + height);
                r.render(g, x, cy, width, mouseX, mouseY, hovered);
            }
            cy += rh;
        }
        g.disableScissor();

        int content = contentHeight();
        if (content > height) {
            int barH = Math.max(20, (int) ((float) height * height / content));
            double frac = scroll / Math.max(1, content - height);
            int barY = y + (int) ((height - barH) * frac);
            g.fill(x + width - 3, y, x + width, y + height, 0x30FFFFFF);
            g.fill(x + width - 3, barY, x + width, barY + barH, 0x90FFFFFF);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!inside(mouseX, mouseY)) {
            return false;
        }
        scroll -= amount * 18.0;
        clamp();
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!inside(mouseX, mouseY)) {
            return false;
        }
        int cy = y - (int) scroll;
        for (Row r : rows) {
            int rh = r.height();
            if (mouseY >= cy && mouseY < cy + rh) {
                return r.click(mouseX, mouseY, x, cy, width);
            }
            cy += rh;
        }
        return false;
    }

    private boolean inside(double mx, double my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
}
