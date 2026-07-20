package dev.nokhxyr.wherewasi.model;

/**
 * A recomputed summary of one play session, written when the session ends.
 * Aggregate counters (blocks mined, mobs killed, deaths, distance) are diffs of
 * the vanilla statistics between the start and the end of the session — the
 * exact numbers the vanilla Statistics screen would show.
 */
public record Session(
        String id,
        String worldId,
        String worldName,
        long startEpochMs,
        long endEpochMs,
        String lastDim,
        int lastX, int lastY, int lastZ,
        String mainZoneId,
        int blocksMined,
        int mobsKilled,
        int deaths,
        long distanceCm,
        int eventCount
) {
    public long durationMs() {
        return Math.max(0L, endEpochMs - startEpochMs);
    }
}
