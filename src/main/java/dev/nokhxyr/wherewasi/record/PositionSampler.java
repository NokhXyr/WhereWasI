package dev.nokhxyr.wherewasi.record;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import dev.nokhxyr.wherewasi.WhereWasIConfig;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.zones.ZoneTracker;

/**
 * Samples position + dimension on an interval to feed the zone tracker and keep
 * the session's "last seen" location current. Also emits a dimension-change event
 * on the tick the player crosses between worlds. Position samples are not stored
 * individually — they only accumulate zone dwell time — so the journal stays lean.
 */
public final class PositionSampler implements Sampler {

    private long lastSampleMs;
    private String lastDim;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        lastSampleMs = ctx.now();
        lastDim = ctx.dimension();
    }

    @Override
    public void tick(RecorderContext ctx) {
        LocalPlayer p = ctx.mc().player;
        if (p == null) {
            return;
        }
        String dim = p.level().dimension().location().toString();

        // Dimension change: checked every tick (cheap string compare), no interval gate.
        if (lastDim != null && !lastDim.equals(dim)) {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("from", lastDim);
            payload.put("to", dim);
            ctx.emit(EventType.DIMENSION_CHANGE, payload);
        }
        lastDim = dim;

        long now = ctx.now();
        long interval = WhereWasIConfig.CONFIG.positionSampleSeconds.get() * 1000L;
        if (now - lastSampleMs < interval) {
            return;
        }
        long elapsed = now - lastSampleMs;
        lastSampleMs = now;

        BlockPos pos = p.blockPosition();
        ctx.recorder().updateLastPosition(dim, pos.getX(), pos.getY(), pos.getZ());

        // Cap elapsed so a paused/AFK gap doesn't dump a huge chunk of time into one cell.
        long capped = Math.min(elapsed, interval * 4L);
        ZoneTracker.Candidate candidate = ctx.zones().accumulate(dim, pos.getX(), pos.getZ(), capped);
        String zoneId = ctx.zones().zoneIdAt(dim, pos.getX(), pos.getZ());
        if (zoneId != null) {
            ctx.recorder().addZoneDwell(zoneId, capped);
        }
        if (candidate != null) {
            ctx.recorder().onZoneCandidate(candidate);
        }
    }
}
