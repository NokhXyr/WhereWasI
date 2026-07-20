package dev.nokhxyr.wherewasi.storage;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.model.Note;
import dev.nokhxyr.wherewasi.model.Session;
import dev.nokhxyr.wherewasi.model.Zone;

/**
 * Explicit, tolerant (de)serialization between the model records and Gson JSON.
 * Written by hand rather than via reflection so the on-disk format is stable and
 * a partially-corrupt line/field degrades to a default instead of throwing.
 */
public final class JournalCodec {

    private JournalCodec() {
    }

    // ---- ActivityEvent -----------------------------------------------------

    public static JsonObject toJson(ActivityEvent e) {
        JsonObject o = new JsonObject();
        o.addProperty("t", e.time());
        o.addProperty("type", e.type().name());
        o.addProperty("dim", e.dim());
        o.addProperty("x", e.x());
        o.addProperty("y", e.y());
        o.addProperty("z", e.z());
        if (e.zoneId() != null) {
            o.addProperty("zone", e.zoneId());
        }
        o.addProperty("imp", e.importance());
        if (!e.payload().isEmpty()) {
            JsonObject p = new JsonObject();
            for (Map.Entry<String, String> en : e.payload().entrySet()) {
                p.addProperty(en.getKey(), en.getValue());
            }
            o.add("p", p);
        }
        return o;
    }

    /** @throws IllegalArgumentException on an unknown/missing event type (caller skips the line). */
    public static ActivityEvent eventFromJson(JsonObject o) {
        EventType type = EventType.valueOf(str(o, "type", ""));
        Map<String, String> payload = new LinkedHashMap<>();
        if (o.has("p") && o.get("p").isJsonObject()) {
            for (Map.Entry<String, JsonElement> en : o.getAsJsonObject("p").entrySet()) {
                if (en.getValue().isJsonPrimitive()) {
                    payload.put(en.getKey(), en.getValue().getAsString());
                }
            }
        }
        return new ActivityEvent(
                lng(o, "t", 0L), type, str(o, "dim", "minecraft:overworld"),
                integer(o, "x", 0), integer(o, "y", 0), integer(o, "z", 0),
                o.has("zone") ? o.get("zone").getAsString() : null,
                integer(o, "imp", type.baseImportance()), payload);
    }

    // ---- Session -----------------------------------------------------------

    public static JsonObject toJson(Session s) {
        JsonObject o = new JsonObject();
        o.addProperty("id", s.id());
        o.addProperty("worldId", s.worldId());
        o.addProperty("worldName", s.worldName());
        o.addProperty("start", s.startEpochMs());
        o.addProperty("end", s.endEpochMs());
        o.addProperty("lastDim", s.lastDim());
        o.addProperty("lx", s.lastX());
        o.addProperty("ly", s.lastY());
        o.addProperty("lz", s.lastZ());
        if (s.mainZoneId() != null) {
            o.addProperty("mainZone", s.mainZoneId());
        }
        o.addProperty("mined", s.blocksMined());
        o.addProperty("kills", s.mobsKilled());
        o.addProperty("deaths", s.deaths());
        o.addProperty("distCm", s.distanceCm());
        o.addProperty("events", s.eventCount());
        return o;
    }

    public static Session sessionFromJson(JsonObject o) {
        return new Session(
                str(o, "id", ""), str(o, "worldId", ""), str(o, "worldName", ""),
                lng(o, "start", 0L), lng(o, "end", 0L),
                str(o, "lastDim", "minecraft:overworld"),
                integer(o, "lx", 0), integer(o, "ly", 0), integer(o, "lz", 0),
                o.has("mainZone") ? o.get("mainZone").getAsString() : null,
                integer(o, "mined", 0), integer(o, "kills", 0), integer(o, "deaths", 0),
                lng(o, "distCm", 0L), integer(o, "events", 0));
    }

    // ---- Zone --------------------------------------------------------------

    public static JsonObject toJson(Zone z) {
        JsonObject o = new JsonObject();
        o.addProperty("id", z.id());
        o.addProperty("name", z.name());
        o.addProperty("dim", z.dim());
        o.addProperty("x0", z.minX());
        o.addProperty("z0", z.minZ());
        o.addProperty("x1", z.maxX());
        o.addProperty("z1", z.maxZ());
        o.addProperty("ms", z.millis());
        return o;
    }

    public static Zone zoneFromJson(JsonObject o) {
        String id = str(o, "id", "");
        String name = str(o, "name", "?");
        String dim = str(o, "dim", "minecraft:overworld");
        long ms = lng(o, "ms", 0L);
        if (o.has("x0")) {
            return new Zone(id, name, dim,
                    integer(o, "x0", 0), integer(o, "z0", 0),
                    integer(o, "x1", 0), integer(o, "z1", 0), ms);
        }
        // Legacy center + radius (radius in 64-block cells) -> bounding box.
        int cx = Math.floorDiv(integer(o, "x", 0), 64);
        int cz = Math.floorDiv(integer(o, "z", 0), 64);
        int r = integer(o, "r", 1);
        return new Zone(id, name, dim,
                (cx - r) * 64, (cz - r) * 64, (cx + r) * 64 + 63, (cz + r) * 64 + 63, ms);
    }

    // ---- Note --------------------------------------------------------------

    public static JsonObject toJson(Note n) {
        JsonObject o = new JsonObject();
        o.addProperty("id", n.id());
        o.addProperty("t", n.time());
        o.addProperty("dim", n.dim());
        o.addProperty("x", n.x());
        o.addProperty("y", n.y());
        o.addProperty("z", n.z());
        if (n.zoneId() != null) {
            o.addProperty("zone", n.zoneId());
        }
        o.addProperty("text", n.text());
        o.addProperty("pinned", n.pinned());
        return o;
    }

    public static Note noteFromJson(JsonObject o) {
        return new Note(
                str(o, "id", ""), lng(o, "t", 0L), str(o, "dim", "minecraft:overworld"),
                integer(o, "x", 0), integer(o, "y", 0), integer(o, "z", 0),
                o.has("zone") ? o.get("zone").getAsString() : null,
                str(o, "text", ""), bool(o, "pinned", false));
    }

    // ---- primitives --------------------------------------------------------

    private static String str(JsonObject o, String k, String def) {
        try {
            return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsString() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static int integer(JsonObject o, String k, int def) {
        try {
            return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsInt() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static long lng(JsonObject o, String k, long def) {
        try {
            return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsLong() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean bool(JsonObject o, String k, boolean def) {
        try {
            return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsBoolean() : def;
        } catch (Exception e) {
            return def;
        }
    }
}
