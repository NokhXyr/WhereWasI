package dev.nokhxyr.wherewasi.ui;

import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.Note;
import dev.nokhxyr.wherewasi.model.Session;
import dev.nokhxyr.wherewasi.model.Zone;

/**
 * The resume briefing: "here's what you were doing." Shows the previous session's
 * age &amp; length, its main zone (with live distance/direction), the top events,
 * deaths, and the pinned note — with "Guide" buttons that light up the HUD arrow.
 */
public final class BriefingScreen extends Screen {

    private final Briefing briefing;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int zoneLineY;
    private int factsY;
    private int summaryY;
    private int deathsY;

    public BriefingScreen(Briefing briefing) {
        super(Component.translatable("wherewasi.briefing.title"));
        this.briefing = briefing;
    }

    @Override
    protected void init() {
        panelW = Math.min(360, width - 40);
        panelX = (width - panelW) / 2;
        panelY = 34;
        panelH = height - 68;

        zoneLineY = panelY + 34;
        if (briefing.mainZone() != null) {
            addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.guide"), b -> guideToZone())
                    .bounds(panelX + panelW - 70, zoneLineY - 4, 62, 16).build());
        }

        factsY = zoneLineY + 22;
        summaryY = factsY + 12 + briefing.topEvents().size() * 20 + 8;
        deathsY = summaryY + summaryHeight();
        if (!briefing.deaths().isEmpty()) {
            addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.guide"), b -> guideToDeath())
                    .bounds(panelX + panelW - 70, deathsY + 6, 62, 16).build());
        }

        int by = panelY + panelH - 26;
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.open_journal"),
                        b -> minecraft.setScreen(new TimelineScreen()))
                .bounds(panelX + 8, by, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.ok"), b -> onClose())
                .bounds(panelX + panelW - 108, by, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, UiText.COL_PANEL);
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, UiText.COL_ACCENT);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        g.drawString(font, title, (width - font.width(title)) / 2, panelY - 14, UiText.COL_TITLE);

        Session last = briefing.last();
        int x = panelX + 10;
        int y = panelY + 8;

        g.drawString(font, Component.translatable("wherewasi.briefing.last_session",
                UiText.relativeTime(last.endEpochMs()), UiText.duration(last.durationMs())), x, y, UiText.COL_TEXT);

        // Where you logged off.
        g.drawString(font, Component.translatable("wherewasi.briefing.last_pos",
                last.lastX(), last.lastY(), last.lastZ()), x, y + 12, UiText.COL_DIM);

        // Zone line with live distance/direction.
        Zone zone = briefing.mainZone();
        Component zoneLine;
        if (zone != null) {
            zoneLine = Component.translatable("wherewasi.briefing.zone", zone.name())
                    .append(" ").append(distanceTo(zone.dim(), zone.centerX(), zone.centerZ()));
        } else {
            zoneLine = Component.translatable("wherewasi.briefing.zone_none");
        }
        g.drawString(font, zoneLine, x, zoneLineY, UiText.COL_TEXT);

        // Highlights.
        g.drawString(font, Component.translatable("wherewasi.briefing.highlights"), x, factsY, UiText.COL_ACCENT);
        int ey = factsY + 12;
        for (ActivityEvent e : briefing.topEvents()) {
            g.renderItem(UiText.iconFor(e), x, ey);
            g.drawString(font, UiText.describe(e), x + 20, ey + 4, UiText.COL_TEXT);
            ey += 20;
        }
        if (briefing.topEvents().isEmpty()) {
            g.drawString(font, Component.translatable("wherewasi.briefing.nothing"), x + 4, ey, UiText.COL_DIM);
        }

        // Session breakdown — what you mined / placed / crafted / killed.
        if (summaryHeight() > 0) {
            int catY = summaryY;
            g.drawString(font, Component.translatable("wherewasi.briefing.activity"), x, catY, UiText.COL_ACCENT);
            catY += 12;
            catY = drawCategory(g, x, catY, "wherewasi.briefing.mined", last.mined(), false);
            catY = drawCategory(g, x, catY, "wherewasi.briefing.placed", last.placed(), false);
            catY = drawCategory(g, x, catY, "wherewasi.briefing.crafted", last.crafted(), false);
            drawCategory(g, x, catY, "wherewasi.briefing.killed", last.killed(), true);
        }

        // Deaths.
        if (!briefing.deaths().isEmpty()) {
            ActivityEvent d = briefing.deaths().get(0);
            String coords = d.x() + ", " + d.y() + ", " + d.z();
            Component deathLine = Component.translatable("wherewasi.briefing.death",
                    briefing.deaths().size(), coords);
            g.drawString(font, deathLine, x, deathsY + 6, UiText.COL_WARN);
        }

        // Pinned note.
        Note pinned = briefing.pinned();
        if (pinned != null) {
            int ny = panelY + panelH - 44;
            g.drawString(font, Component.translatable("wherewasi.briefing.pinned", pinned.text()), x, ny, UiText.COL_ACCENT);
        }
    }

    private int summaryHeight() {
        Session last = briefing.last();
        int categories = 0;
        if (!last.mined().isEmpty()) {
            categories++;
        }
        if (!last.placed().isEmpty()) {
            categories++;
        }
        if (!last.crafted().isEmpty()) {
            categories++;
        }
        if (!last.killed().isEmpty()) {
            categories++;
        }
        return categories > 0 ? 12 + categories * 10 + 4 : 0;
    }

    private int drawCategory(GuiGraphics g, int x, int y, String key, Map<String, Integer> counts, boolean entity) {
        if (counts.isEmpty()) {
            return y;
        }
        Component line = Component.translatable(key, UiText.summarizeCounts(counts, entity, 4));
        g.enableScissor(x, y - 1, panelX + panelW - 10, y + 9);
        g.drawString(font, line, x, y, UiText.COL_TEXT);
        g.disableScissor();
        return y + 10;
    }

    private Component distanceTo(String dim, int tx, int tz) {
        LocalPlayer p = minecraft == null ? null : minecraft.player;
        if (p == null) {
            return Component.empty();
        }
        if (!p.level().dimension().location().toString().equals(dim)) {
            return Component.translatable("wherewasi.hud.other_dim");
        }
        double dx = tx + 0.5 - p.getX();
        double dz = tz + 0.5 - p.getZ();
        return Component.translatable("wherewasi.briefing.distance",
                UiText.horizontalDistance(dx, dz), UiText.directionName(dx, dz));
    }

    private void guideToZone() {
        Zone z = briefing.mainZone();
        if (z != null) {
            ClientState.setGuideTarget(new GuideTarget(z.dim(), z.centerX(), 64, z.centerZ(), z.name()));
        }
        onClose();
    }

    private void guideToDeath() {
        ActivityEvent d = briefing.deaths().get(0);
        ClientState.setGuideTarget(new GuideTarget(d.dim(), d.x(), d.y(), d.z(),
                Component.translatable("wherewasi.briefing.death_marker").getString()));
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
