package dev.nokhxyr.wherewasi.model;

/**
 * A manual, timestamped note bound to the position/dimension/zone where it was
 * written. At most one note is "pinned" at a time; the pinned note shows in the
 * briefing and (optionally) on the HUD.
 */
public record Note(
        String id,
        long time,
        String dim,
        int x, int y, int z,
        String zoneId,
        String text,
        boolean pinned
) {
    public Note withPinned(boolean p) {
        return new Note(id, time, dim, x, y, z, zoneId, text, p);
    }
}
