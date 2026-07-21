package dev.nokhxyr.wherewasi.ui;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.Session;

/**
 * Shared formatting for the UI: localized time/duration/direction strings, icons
 * and one-line descriptions for events. Every player-facing string routes through
 * a lang key so both en_us and fr_fr are covered.
 */
public final class UiText {

    public static final int COL_TITLE = 0xFFFFFFFF;
    public static final int COL_TEXT = 0xFFDDDDDD;
    public static final int COL_DIM = 0xFF9AA0A6;
    public static final int COL_ACCENT = 0xFF6CA0F6;
    public static final int COL_WARN = 0xFFE0703C;
    public static final int COL_PANEL = 0xC00E0E14;
    public static final int COL_PANEL_LIGHT = 0x30FFFFFF;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private UiText() {
    }

    // ---- time --------------------------------------------------------------

    public static Component relativeTime(long epochMs) {
        long delta = Math.max(0L, System.currentTimeMillis() - epochMs);
        long minutes = delta / 60_000L;
        if (minutes < 1) {
            return Component.translatable("wherewasi.time.just_now");
        }
        if (minutes < 60) {
            return Component.translatable("wherewasi.time.minutes_ago", minutes);
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return Component.translatable("wherewasi.time.hours_ago", hours);
        }
        return Component.translatable("wherewasi.time.days_ago", hours / 24);
    }

    public static Component duration(long ms) {
        long minutes = Math.max(0L, ms) / 60_000L;
        long h = minutes / 60;
        long m = minutes % 60;
        return h > 0
                ? Component.translatable("wherewasi.duration.hm", h, m)
                : Component.translatable("wherewasi.duration.m", m);
    }

    public static String dayLabel(long epochMs) {
        return DAY.format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()));
    }

    public static String clock(long epochMs) {
        return HHMM.format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()));
    }

    // ---- direction / distance ---------------------------------------------

    /** 0=E,1=SE,2=S,3=SW,4=W,5=NW,6=N,7=NE (Minecraft: +x east, +z south). */
    public static int octant(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        return (int) Math.floorMod(Math.round(angle / 45.0), 8L);
    }

    private static final String[] OCTANT_KEYS = {"e", "se", "s", "sw", "w", "nw", "n", "ne"};
    private static final String[] OCTANT_ARROWS = {"→", "↘", "↓", "↙", "←", "↖", "↑", "↗"};

    public static Component directionName(double dx, double dz) {
        return Component.translatable("wherewasi.dir." + OCTANT_KEYS[octant(dx, dz)]);
    }

    public static String arrow(int octant) {
        return OCTANT_ARROWS[Math.floorMod(octant, 8)];
    }

    public static int horizontalDistance(double dx, double dz) {
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }

    // ---- events ------------------------------------------------------------

    public static Component dimensionName(String dimId) {
        if (dimId == null) {
            return Component.literal("?");
        }
        ResourceLocation rl = ResourceLocation.tryParse(dimId);
        String path = rl == null ? dimId : rl.getPath();
        return switch (path) {
            case "overworld" -> Component.translatable("wherewasi.dim.overworld");
            case "the_nether" -> Component.translatable("wherewasi.dim.the_nether");
            case "the_end" -> Component.translatable("wherewasi.dim.the_end");
            default -> Component.literal(path.replace('_', ' '));
        };
    }

    public static Component itemName(String id) {
        ResourceLocation rl = id == null ? null : ResourceLocation.tryParse(id);
        if (rl == null) {
            return Component.literal(String.valueOf(id));
        }
        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item == Items.AIR) {
            return Component.literal(rl.getPath().replace('_', ' '));
        }
        return new ItemStack(item).getHoverName();
    }

    public static ItemStack iconFor(ActivityEvent e) {
        return switch (e.type()) {
            case FIRST_ACQUIRE, BULK_ACQUIRE -> stackOr(e.get("item"), Items.CHEST);
            case MINE_MILESTONE -> stackOr(e.get("block"), Items.IRON_PICKAXE);
            case DEATH -> new ItemStack(Items.SKELETON_SKULL);
            case ADVANCEMENT -> new ItemStack(Items.NETHER_STAR);
            case DIMENSION_CHANGE -> new ItemStack(Items.ENDER_PEARL);
            case BIOME_ENTER -> new ItemStack(Items.FILLED_MAP);
            case NOTE -> new ItemStack(Items.WRITABLE_BOOK);
            case ZONE_NAMED -> new ItemStack(Items.OAK_SIGN);
            case ZONE_ACTIVITY -> new ItemStack(Items.COMPASS);
            case SEGMENT -> new ItemStack(segmentIcon(e.get("activity")));
            case SESSION_START, SESSION_END -> new ItemStack(Items.CLOCK);
        };
    }

    private static Item segmentIcon(String activity) {
        if (activity == null) {
            return Items.CLOCK;
        }
        return switch (activity) {
            case "mining" -> Items.IRON_PICKAXE;
            case "building" -> Items.BRICKS;
            case "combat" -> Items.IRON_SWORD;
            case "exploring" -> Items.COMPASS;
            default -> Items.CLOCK;
        };
    }

    public static Component describe(ActivityEvent e) {
        return switch (e.type()) {
            case FIRST_ACQUIRE -> Component.translatable("wherewasi.event.first_acquire", itemName(e.get("item")));
            case BULK_ACQUIRE -> Component.translatable("wherewasi.event.bulk_acquire",
                    e.getInt("count", 0), itemName(e.get("item")));
            case MINE_MILESTONE -> Component.translatable("wherewasi.event.mine", itemName(e.get("block")));
            case DEATH -> e.get("cause") != null
                    ? Component.literal(e.get("cause"))
                    : Component.translatable("wherewasi.event.death");
            case ADVANCEMENT -> Component.translatable("wherewasi.event.advancement",
                    e.get("title") != null ? e.get("title") : e.get("adv"));
            case DIMENSION_CHANGE -> Component.translatable("wherewasi.event.dimension", dimensionName(e.get("to")));
            case BIOME_ENTER -> Component.translatable("wherewasi.event.biome_enter", biomeName(e.get("biome")));
            case SEGMENT -> segmentLine(e);
            case NOTE -> Component.translatable("wherewasi.event.note", e.get("text") != null ? e.get("text") : "");
            case ZONE_NAMED -> Component.translatable("wherewasi.event.zone_named", e.get("name") != null ? e.get("name") : "");
            case ZONE_ACTIVITY -> Component.translatable("wherewasi.event.zone_activity",
                    e.getInt("mined", 0), e.getInt("kills", 0));
            case SESSION_START -> Component.translatable("wherewasi.event.session_start");
            case SESSION_END -> e.get("summary") != null
                    ? Component.translatable("wherewasi.event.session_end_summary", e.get("summary"))
                    : Component.translatable("wherewasi.event.session_end");
        };
    }

    // ---- timeline ----------------------------------------------------------

    /** "Today" / "Yesterday" / an ISO date, for grouping the timeline by day. */
    public static Component dayHeading(long epochMs) {
        LocalDate day = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        if (day.equals(today)) {
            return Component.translatable("wherewasi.day.today");
        }
        if (day.equals(today.minusDays(1))) {
            return Component.translatable("wherewasi.day.yesterday");
        }
        return Component.literal(DAY.format(day));
    }

    public static Component biomeName(String id) {
        ResourceLocation rl = id == null ? null : ResourceLocation.tryParse(id);
        if (rl == null) {
            return Component.literal(String.valueOf(id));
        }
        return Component.translatable("biome." + rl.getNamespace() + "." + rl.getPath());
    }

    /** One-line description of an activity-chapter event: "Mining — 240 mined · 3 killed". */
    public static Component segmentLine(ActivityEvent e) {
        String activity = e.get("activity");
        Component label = Component.translatable("wherewasi.segment." + (activity == null ? "exploring" : activity));
        MutableComponent stats = statsSummary(e.getInt("mined", 0), e.getInt("placed", 0),
                e.getInt("killed", 0), 0, e.getInt("distM", 0));
        return Component.translatable("wherewasi.event.segment", label, stats);
    }

    /** Compact "512 mined · 14 killed · 3 deaths · 1200m" line for a session header. */
    public static Component sessionHeadline(Session s) {
        int placed = 0;
        for (int v : s.placed().values()) {
            placed += v;
        }
        return statsSummary(s.blocksMined(), placed, s.mobsKilled(), s.deaths(), (int) (s.distanceCm() / 100L));
    }

    /** Joins the non-zero counters with " · " separators; empty when nothing happened. */
    public static MutableComponent statsSummary(int mined, int placed, int killed, int deaths, int distM) {
        MutableComponent out = Component.empty();
        int n = 0;
        n = appendStat(out, n, "wherewasi.stat.mined", mined);
        n = appendStat(out, n, "wherewasi.stat.placed", placed);
        n = appendStat(out, n, "wherewasi.stat.killed", killed);
        n = appendStat(out, n, "wherewasi.stat.deaths", deaths);
        appendStat(out, n, "wherewasi.stat.dist", distM);
        return out;
    }

    private static int appendStat(MutableComponent out, int count, String key, int value) {
        if (value <= 0) {
            return count;
        }
        if (count > 0) {
            out.append(" · ");
        }
        out.append(Component.translatable(key, value));
        return count + 1;
    }

    // ---- session breakdown -------------------------------------------------

    /** Localized "128 Stone, 34 Iron Ore, 5 Diamond Ore" for a briefing line. */
    public static Component summarizeCounts(Map<String, Integer> counts, boolean entity, int max) {
        MutableComponent out = Component.empty();
        int i = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (i >= max) {
                out.append(", ...");
                break;
            }
            if (i > 0) {
                out.append(", ");
            }
            out.append(Component.literal(e.getValue() + " "))
                    .append(entity ? entityName(e.getKey()) : itemName(e.getKey()));
            i++;
        }
        return out;
    }

    /** Compact plain-text summary used for the session-end journal entry. */
    public static String sessionSummary(Map<String, Integer> mined, Map<String, Integer> killed, Map<String, Integer> crafted) {
        StringBuilder sb = new StringBuilder();
        appendCounts(sb, mined, false, 3);
        appendCounts(sb, killed, true, 2);
        appendCounts(sb, crafted, false, 2);
        return sb.toString();
    }

    private static void appendCounts(StringBuilder sb, Map<String, Integer> counts, boolean entity, int max) {
        int i = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (i >= max) {
                break;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            String name = (entity ? entityName(e.getKey()) : itemName(e.getKey())).getString();
            sb.append(e.getValue()).append(" ").append(name);
            i++;
        }
    }

    public static Component entityName(String id) {
        ResourceLocation rl = id == null ? null : ResourceLocation.tryParse(id);
        if (rl == null) {
            return Component.literal(String.valueOf(id));
        }
        return BuiltInRegistries.ENTITY_TYPE.get(rl).getDescription();
    }

    private static ItemStack stackOr(String id, Item fallback) {
        ResourceLocation rl = id == null ? null : ResourceLocation.tryParse(id);
        if (rl != null) {
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        return new ItemStack(fallback);
    }
}
