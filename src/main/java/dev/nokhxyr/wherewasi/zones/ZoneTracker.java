package dev.nokhxyr.wherewasi.zones;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.nokhxyr.wherewasi.WhereWasIConfig;
import dev.nokhxyr.wherewasi.model.Zone;
import dev.nokhxyr.wherewasi.storage.JournalCodec;
import dev.nokhxyr.wherewasi.storage.JournalStorage;

/**
 * Accumulates dwell time per 64x64 cell (per dimension) and clusters it into
 * named {@link Zone}s. When an unnamed cell crosses the configured threshold it
 * becomes a {@link Candidate} the UI can offer to name. Zone tagging allows a
 * one-cell radius so a base spanning a few cells still reads as one place.
 */
public final class ZoneTracker {

    private static final String FILE = "zones.json";

    public record Candidate(String dim, int x, int z, long millis) {
    }

    private final Map<String, Map<Long, Long>> cellTime = new HashMap<>();
    private final Set<String> prompted = new LinkedHashSet<>();
    private final List<Zone> zones = new ArrayList<>();

    // ---- accumulation ------------------------------------------------------

    /**
     * Adds dwell time to the cell containing (x,z). Returns a fresh candidate to
     * propose (toast) the first time a cell crosses the threshold, else null.
     */
    public Candidate accumulate(String dim, int x, int z, long millis) {
        if (millis <= 0) {
            return null;
        }
        int cx = Math.floorDiv(x, 64);
        int cz = Math.floorDiv(z, 64);
        long k = key(cx, cz);
        Map<Long, Long> cells = cellTime.computeIfAbsent(dim, d -> new HashMap<>());
        long total = cells.getOrDefault(k, 0L) + millis;
        cells.put(k, total);

        long threshold = thresholdMillis();
        String promptKey = dim + ":" + k;
        if (total >= threshold && !prompted.contains(promptKey) && zoneAt(dim, cx * 64 + 32, cz * 64 + 32) == null) {
            prompted.add(promptKey);
            return new Candidate(dim, cx * 64 + 32, cz * 64 + 32, total);
        }
        return null;
    }

    /** Hot, still-unnamed cells the management screen can offer to name. */
    public List<Candidate> candidates() {
        long threshold = thresholdMillis();
        List<Candidate> out = new ArrayList<>();
        for (Map.Entry<String, Map<Long, Long>> byDim : cellTime.entrySet()) {
            String dim = byDim.getKey();
            for (Map.Entry<Long, Long> cell : byDim.getValue().entrySet()) {
                if (cell.getValue() < threshold) {
                    continue;
                }
                int cx = keyX(cell.getKey());
                int cz = keyZ(cell.getKey());
                int wx = cx * 64 + 32;
                int wz = cz * 64 + 32;
                if (zoneAt(dim, wx, wz) == null) {
                    out.add(new Candidate(dim, wx, wz, cell.getValue()));
                }
            }
        }
        out.sort(Comparator.comparingLong(Candidate::millis).reversed());
        return out;
    }

    // ---- zones -------------------------------------------------------------

    public List<Zone> zones() {
        return zones;
    }

    public Zone zoneAt(String dim, int x, int z) {
        Zone best = null;
        for (Zone zone : zones) {
            if (zone.covers(dim, x, z) && (best == null || zone.areaBlocks() < best.areaBlocks())) {
                best = zone;
            }
        }
        return best;
    }

    public String zoneIdAt(String dim, int x, int z) {
        Zone zone = zoneAt(dim, x, z);
        return zone == null ? null : zone.id();
    }

    public Zone zoneById(String id) {
        if (id == null) {
            return null;
        }
        for (Zone z : zones) {
            if (z.id().equals(id)) {
                return z;
            }
        }
        return null;
    }

    public Zone nameCandidate(Candidate c, String name) {
        int cx = Math.floorDiv(c.x(), 64);
        int cz = Math.floorDiv(c.z(), 64);
        Zone zone = new Zone(newId(), name, c.dim(), cx * 64, cz * 64, cx * 64 + 63, cz * 64 + 63, c.millis());
        zones.add(zone);
        return zone;
    }

    /** Claims the single 64x64 cell at the given position (the quick "here" claim). */
    public Zone createZoneAt(String dim, int x, int z, String name) {
        int cx = Math.floorDiv(x, 64);
        int cz = Math.floorDiv(z, 64);
        long millis = cellTime.getOrDefault(dim, Map.of()).getOrDefault(key(cx, cz), 0L);
        Zone zone = new Zone(newId(), name, dim, cx * 64, cz * 64, cx * 64 + 63, cz * 64 + 63, millis);
        zones.add(zone);
        prompted.add(dim + ":" + key(cx, cz));
        return zone;
    }

    /**
     * Claims every chunk between two world points (a two-corner "claim" selection) as one
     * zone. The box is snapped to whole chunks; dwell time already accumulated in the
     * covered area is carried over.
     */
    public Zone createClaim(String dim, int x1, int z1, int x2, int z2, String name) {
        int[] box = Zone.boxFromPoints(x1, z1, x2, z2);
        Zone zone = new Zone(newId(), name, dim, box[0], box[1], box[2], box[3],
                sumCellTime(dim, box[0], box[1], box[2], box[3]));
        zones.add(zone);
        markPrompted(dim, box);
        return zone;
    }

    /** Grows (delta &gt; 0) or shrinks (delta &lt; 0) a claim by whole chunks on every side. */
    public void resize(String id, int delta) {
        int d = delta * 16;
        for (int i = 0; i < zones.size(); i++) {
            Zone z = zones.get(i);
            if (z.id().equals(id)) {
                int nMinX = z.minX() - d;
                int nMinZ = z.minZ() - d;
                int nMaxX = z.maxX() + d;
                int nMaxZ = z.maxZ() + d;
                if (nMaxX >= nMinX && nMaxZ >= nMinZ) {
                    zones.set(i, z.withBounds(nMinX, nMinZ, nMaxX, nMaxZ));
                }
                return;
            }
        }
    }

    public void rename(String id, String newName) {
        for (int i = 0; i < zones.size(); i++) {
            if (zones.get(i).id().equals(id)) {
                zones.set(i, zones.get(i).withName(newName));
                return;
            }
        }
    }

    public void delete(String id) {
        zones.removeIf(z -> z.id().equals(id));
    }

    /** Folds {@code otherId} into {@code intoId}: keeps the target's name/anchor, sums dwell time. */
    public void merge(String intoId, String otherId) {
        Zone into = zoneById(intoId);
        Zone other = zoneById(otherId);
        if (into == null || other == null || intoId.equals(otherId)) {
            return;
        }
        Zone merged = new Zone(into.id(), into.name(), into.dim(),
                Math.min(into.minX(), other.minX()), Math.min(into.minZ(), other.minZ()),
                Math.max(into.maxX(), other.maxX()), Math.max(into.maxZ(), other.maxZ()),
                into.millis() + other.millis());
        for (int i = 0; i < zones.size(); i++) {
            if (zones.get(i).id().equals(intoId)) {
                zones.set(i, merged);
            }
        }
        zones.removeIf(z -> z.id().equals(otherId));
    }

    // ---- persistence -------------------------------------------------------

    public void load(JournalStorage storage) {
        cellTime.clear();
        prompted.clear();
        zones.clear();
        JsonElement el = storage.readJson(FILE);
        if (el == null || !el.isJsonObject()) {
            return;
        }
        JsonObject root = el.getAsJsonObject();
        if (root.has("zones") && root.get("zones").isJsonArray()) {
            for (JsonElement e : root.getAsJsonArray("zones")) {
                if (e.isJsonObject()) {
                    zones.add(JournalCodec.zoneFromJson(e.getAsJsonObject()));
                }
            }
        }
        if (root.has("cells") && root.get("cells").isJsonObject()) {
            for (Map.Entry<String, JsonElement> byDim : root.getAsJsonObject("cells").entrySet()) {
                if (!byDim.getValue().isJsonObject()) {
                    continue;
                }
                Map<Long, Long> cells = new HashMap<>();
                for (Map.Entry<String, JsonElement> cell : byDim.getValue().getAsJsonObject().entrySet()) {
                    try {
                        cells.put(Long.parseLong(cell.getKey()), cell.getValue().getAsLong());
                    } catch (Exception ignored) {
                        // skip corrupt cell
                    }
                }
                cellTime.put(byDim.getKey(), cells);
            }
        }
        if (root.has("prompted") && root.get("prompted").isJsonArray()) {
            for (JsonElement e : root.getAsJsonArray("prompted")) {
                try {
                    prompted.add(e.getAsString());
                } catch (Exception ignored) {
                    // skip
                }
            }
        }
    }

    public void save(JournalStorage storage) {
        JsonObject root = new JsonObject();
        JsonArray zoneArr = new JsonArray();
        for (Zone z : zones) {
            zoneArr.add(JournalCodec.toJson(z));
        }
        root.add("zones", zoneArr);

        JsonObject cellsObj = new JsonObject();
        for (Map.Entry<String, Map<Long, Long>> byDim : cellTime.entrySet()) {
            JsonObject dimObj = new JsonObject();
            for (Map.Entry<Long, Long> cell : byDim.getValue().entrySet()) {
                // Persist only cells worth remembering to keep the file small.
                if (cell.getValue() >= 60_000L) {
                    dimObj.addProperty(Long.toString(cell.getKey()), cell.getValue());
                }
            }
            if (!dimObj.isEmpty()) {
                cellsObj.add(byDim.getKey(), dimObj);
            }
        }
        root.add("cells", cellsObj);

        JsonArray promptedArr = new JsonArray();
        for (String s : prompted) {
            promptedArr.add(s);
        }
        root.add("prompted", promptedArr);

        storage.writeJson(FILE, root);
    }

    // ---- helpers -----------------------------------------------------------

    private String newId() {
        return "zone_" + Integer.toHexString(zones.size()) + "_" + Long.toHexString(System.currentTimeMillis());
    }

    private long sumCellTime(String dim, int minX, int minZ, int maxX, int maxZ) {
        Map<Long, Long> cells = cellTime.get(dim);
        if (cells == null) {
            return 0L;
        }
        long total = 0L;
        for (Map.Entry<Long, Long> e : cells.entrySet()) {
            int wx = keyX(e.getKey()) * 64 + 32;
            int wz = keyZ(e.getKey()) * 64 + 32;
            if (wx >= minX && wx <= maxX && wz >= minZ && wz <= maxZ) {
                total += e.getValue();
            }
        }
        return total;
    }

    private void markPrompted(String dim, int[] box) {
        for (int cx = Math.floorDiv(box[0], 64); cx <= Math.floorDiv(box[2], 64); cx++) {
            for (int cz = Math.floorDiv(box[1], 64); cz <= Math.floorDiv(box[3], 64); cz++) {
                prompted.add(dim + ":" + key(cx, cz));
            }
        }
    }

    private static long thresholdMillis() {
        return WhereWasIConfig.CONFIG.zoneThresholdMinutes.get() * 60_000L;
    }

    private static long key(int cellX, int cellZ) {
        return (((long) cellX) & 0xFFFFFFFFL) | (((long) cellZ) << 32);
    }

    private static int keyX(long k) {
        return (int) (k & 0xFFFFFFFFL);
    }

    private static int keyZ(long k) {
        return (int) (k >> 32);
    }
}
