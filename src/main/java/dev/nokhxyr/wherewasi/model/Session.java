package dev.nokhxyr.wherewasi.model;

import java.util.Map;

/**
 * A recomputed summary of one play session, written when the session ends.
 * Aggregate counters and the per-type breakdowns (blocks mined/placed, items
 * crafted, mobs killed) are diffs of the vanilla statistics between the start and
 * the end of the session — the exact numbers the vanilla Statistics screen shows.
 * The breakdown maps are {@code id -> count}, already trimmed to the top entries.
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
        int eventCount,
        Map<String, Integer> mined,
        Map<String, Integer> placed,
        Map<String, Integer> crafted,
        Map<String, Integer> killed
) {
    public Session {
        mined = mined == null ? Map.of() : mined;
        placed = placed == null ? Map.of() : placed;
        crafted = crafted == null ? Map.of() : crafted;
        killed = killed == null ? Map.of() : killed;
    }

    public long durationMs() {
        return Math.max(0L, endEpochMs - startEpochMs);
    }

    public boolean hasBreakdown() {
        return !mined.isEmpty() || !placed.isEmpty() || !crafted.isEmpty() || !killed.isEmpty();
    }
}
