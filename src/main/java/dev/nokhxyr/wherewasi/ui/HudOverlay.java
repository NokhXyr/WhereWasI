package dev.nokhxyr.wherewasi.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import dev.nokhxyr.wherewasi.WhereWasIConfig;
import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.Note;

/**
 * A tiny in-game HUD widget: the pinned note and, when a guide target is set, a
 * compass-style arrow with the bearing and distance to it. Home-grown, no minimap
 * dependency. Anchored to the configured screen corner.
 */
public final class HudOverlay implements LayeredDraw.Layer {

    private static final int PAD = 4;
    private static final int LINE_H = 10;

    @Override
    public void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        List<Component> lines = new ArrayList<>();

        if (WhereWasIConfig.CONFIG.hudGuide.get()) {
            GuideTarget target = ClientState.guideTarget();
            if (target != null) {
                lines.add(guideLine(player, target));
            }
        }
        if (WhereWasIConfig.CONFIG.hudPinnedNote.get() && ClientState.recorder().sessionActive()) {
            Note pinned = ClientState.recorder().pinnedNote();
            if (pinned != null) {
                lines.add(Component.translatable("wherewasi.hud.note", trim(pinned.text(), 42)));
            }
        }
        if (lines.isEmpty()) {
            return;
        }
        drawPanel(g, mc, lines);
    }

    private Component guideLine(LocalPlayer player, GuideTarget target) {
        String dim = player.level().dimension().location().toString();
        String label = target.label() == null || target.label().isBlank()
                ? Component.translatable("wherewasi.hud.target").getString()
                : target.label();
        if (!dim.equals(target.dim())) {
            return Component.literal("» " + label + " ").append(Component.translatable("wherewasi.hud.other_dim"));
        }
        double dx = target.x() + 0.5 - player.getX();
        double dz = target.z() + 0.5 - player.getZ();
        int dist = UiText.horizontalDistance(dx, dz);
        int oct = UiText.octant(dx, dz);
        return Component.literal(UiText.arrow(oct) + " " + label + "  " + dist + "m ")
                .append(UiText.directionName(dx, dz));
    }

    private void drawPanel(GuiGraphics g, Minecraft mc, List<Component> lines) {
        Font font = mc.font;
        int textW = 0;
        for (Component c : lines) {
            textW = Math.max(textW, font.width(c));
        }
        int boxW = textW + PAD * 2;
        int boxH = lines.size() * LINE_H + PAD * 2;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int x;
        int y;
        switch (WhereWasIConfig.CONFIG.hudCorner.get()) {
            case TOP_RIGHT -> {
                x = sw - boxW - 4;
                y = 4;
            }
            case BOTTOM_LEFT -> {
                x = 4;
                y = sh - boxH - 4;
            }
            case BOTTOM_RIGHT -> {
                x = sw - boxW - 4;
                y = sh - boxH - 4;
            }
            default -> {
                x = 4;
                y = 4;
            }
        }

        g.fill(x, y, x + boxW, y + boxH, UiText.COL_PANEL);
        int ly = y + PAD;
        for (Component c : lines) {
            g.drawString(font, c, x + PAD, ly, UiText.COL_ACCENT);
            ly += LINE_H;
        }
    }

    private static String trim(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
