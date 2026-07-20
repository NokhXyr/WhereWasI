package dev.nokhxyr.wherewasi.record;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.model.Importance;
import dev.nokhxyr.wherewasi.storage.JournalStorage;
import dev.nokhxyr.wherewasi.zones.ZoneTracker;

/**
 * The narrow surface a {@link Sampler} is handed each tick: the clock, the game,
 * and an {@link #emit} sink that stamps the event with the player's current
 * position, dimension, covering zone and importance score before journaling it.
 */
public final class RecorderContext {

    private final Minecraft mc;
    private final ActivityRecorder recorder;

    RecorderContext(Minecraft mc, ActivityRecorder recorder) {
        this.mc = mc;
        this.recorder = recorder;
    }

    public Minecraft mc() {
        return mc;
    }

    public ActivityRecorder recorder() {
        return recorder;
    }

    public ZoneTracker zones() {
        return recorder.zones();
    }

    public JournalStorage storage() {
        return recorder.storage();
    }

    public long now() {
        return System.currentTimeMillis();
    }

    public String dimension() {
        LocalPlayer p = mc.player;
        return p == null ? null : p.level().dimension().location().toString();
    }

    /**
     * Builds and journals an event at the player's current location. Silently
     * does nothing if the player is gone (e.g. mid-disconnect).
     */
    public void emit(EventType type, Map<String, String> payload) {
        LocalPlayer p = mc.player;
        if (p == null) {
            return;
        }
        String dim = p.level().dimension().location().toString();
        BlockPos pos = p.blockPosition();
        String zoneId = recorder.zones().zoneIdAt(dim, pos.getX(), pos.getZ());
        int importance = Importance.score(type, payload);
        recorder.record(new ActivityEvent(now(), type, dim,
                pos.getX(), pos.getY(), pos.getZ(), zoneId, importance, payload));
    }
}
