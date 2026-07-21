package dev.nokhxyr.wherewasi.record;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import dev.nokhxyr.wherewasi.WhereWasIConfig;
import dev.nokhxyr.wherewasi.model.EventType;

/**
 * Turns each stretch of play into a readable "chapter" on the timeline. On a fixed
 * cadence (see {@code segmentMinutes}) it diffs the same vanilla statistics the
 * {@link StatsPoller} keeps warm, classifies the dominant activity of the window
 * (mining / building / exploring / combat) and emits a {@link EventType#SEGMENT}
 * event summarising it. Idle windows (nothing meaningful happened) are skipped so
 * the timeline stays clean; the trailing partial window is flushed on session end.
 */
public final class SegmentSampler implements Sampler {

    private static final long WARMUP_MS = 5000L;
    private static final long MIN_TAIL_MS = 60_000L;

    private long sessionStartMs;
    private long windowStartMs;
    private boolean armed;

    private long minedAtStart;
    private long placedAtStart;
    private long distAtStart;
    private int killsAtStart;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        sessionStartMs = ctx.now();
        windowStartMs = ctx.now();
        armed = false;
    }

    @Override
    public void tick(RecorderContext ctx) {
        LocalPlayer p = ctx.mc().player;
        if (p == null) {
            return;
        }
        long now = ctx.now();

        // Wait for the vanilla stats to be warm (StatsPoller requests them on join)
        // before snapshotting, otherwise the first window would count lifetime totals.
        if (!armed) {
            if (now - sessionStartMs >= WARMUP_MS) {
                snapshot(p.getStats());
                windowStartMs = now;
                armed = true;
            }
            return;
        }

        long windowMs = WhereWasIConfig.CONFIG.segmentMinutes.get() * 60_000L;
        if (now - windowStartMs < windowMs) {
            return;
        }
        emitSegment(ctx, p.getStats());
        snapshot(p.getStats());
        windowStartMs = now;
    }

    @Override
    public void onSessionEnd(RecorderContext ctx) {
        LocalPlayer p = ctx.mc().player;
        if (p != null && armed && ctx.now() - windowStartMs >= MIN_TAIL_MS) {
            emitSegment(ctx, p.getStats());
        }
    }

    private void snapshot(StatsCounter s) {
        minedAtStart = totalMined(s);
        placedAtStart = totalPlaced(s);
        distAtStart = totalDistanceCm(s);
        killsAtStart = s.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
    }

    private void emitSegment(RecorderContext ctx, StatsCounter s) {
        int mined = (int) Math.max(0L, totalMined(s) - minedAtStart);
        int placed = (int) Math.max(0L, totalPlaced(s) - placedAtStart);
        int killed = Math.max(0, s.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS)) - killsAtStart);
        int distM = (int) Math.max(0L, (totalDistanceCm(s) - distAtStart) / 100L);

        if (mined < 15 && placed < 15 && killed < 3 && distM < 120) {
            return; // idle window — don't clutter the timeline
        }

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("activity", classify(mined, placed, killed, distM));
        putIfPositive(payload, "mined", mined);
        putIfPositive(payload, "placed", placed);
        putIfPositive(payload, "killed", killed);
        putIfPositive(payload, "distM", distM);
        ctx.emit(EventType.SEGMENT, payload);
    }

    /** Weighted argmax so one kill or one metre doesn't outrank a mining spree. */
    private static String classify(int mined, int placed, int killed, int distM) {
        int mining = mined;
        int building = placed;
        int combat = killed * 20;
        int exploring = distM / 5;
        int max = Math.max(Math.max(mining, building), Math.max(combat, exploring));
        if (max == building && building >= mining) {
            return "building";
        }
        if (max == combat && combat >= mining) {
            return "combat";
        }
        if (max == mining) {
            return "mining";
        }
        return "exploring";
    }

    private static void putIfPositive(Map<String, String> payload, String key, int value) {
        if (value > 0) {
            payload.put(key, Integer.toString(value));
        }
    }

    // ---- vanilla stat reads (same source of truth as the vanilla Statistics screen) ----

    private static long totalMined(StatsCounter s) {
        long total = 0L;
        for (Block b : BuiltInRegistries.BLOCK) {
            total += s.getValue(Stats.BLOCK_MINED.get(b));
        }
        return total;
    }

    private static long totalPlaced(StatsCounter s) {
        long total = 0L;
        for (Item it : BuiltInRegistries.ITEM) {
            if (it instanceof BlockItem) {
                total += s.getValue(Stats.ITEM_USED.get(it));
            }
        }
        return total;
    }

    private static long totalDistanceCm(StatsCounter s) {
        return s.getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM))
                + s.getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM))
                + s.getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM))
                + s.getValue(Stats.CUSTOM.get(Stats.SWIM_ONE_CM))
                + s.getValue(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM));
    }
}
