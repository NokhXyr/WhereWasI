package dev.nokhxyr.wherewasi.model;

import java.util.Map;

/**
 * One journalled moment. Immutable; serialized one-per-line as JSON.
 *
 * <p>Type-specific details live in {@link #payload()} as small string entries
 * (for example {@code item -> minecraft:diamond}, {@code count -> 5}) so the
 * on-disk format stays flat and the display text can be (re)localized at render
 * time rather than being baked in.
 */
public record ActivityEvent(
        long time,
        EventType type,
        String dim,
        int x, int y, int z,
        String zoneId,
        int importance,
        Map<String, String> payload
) {
    public ActivityEvent {
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }

    public String get(String key) {
        return payload.get(key);
    }

    public int getInt(String key, int fallback) {
        String v = payload.get(key);
        if (v == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
