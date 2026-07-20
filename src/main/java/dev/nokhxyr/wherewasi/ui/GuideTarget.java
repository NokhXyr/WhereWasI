package dev.nokhxyr.wherewasi.ui;

/**
 * A world location the HUD points the player toward, set from the briefing's
 * "Guide" buttons. Lightweight and home-grown — no minimap dependency.
 */
public record GuideTarget(String dim, int x, int y, int z, String label) {
}
