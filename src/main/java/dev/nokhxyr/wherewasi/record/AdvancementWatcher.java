package dev.nokhxyr.wherewasi.record;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;

import dev.nokhxyr.wherewasi.model.EventType;

/**
 * Watches the client's advancement progress map for newly-completed advancements.
 * Advancements already done when the player joined are seeded as "seen" so only
 * genuine new completions during the session are logged. Recipe advancements
 * (which have no display) are ignored.
 */
public final class AdvancementWatcher implements Sampler {

    private final Set<String> seen = new HashSet<>();
    private boolean seeded;
    private long sessionStartMs;
    private long lastPollMs;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        seen.clear();
        seeded = false;
        sessionStartMs = ctx.now();
        lastPollMs = 0L;
    }

    @Override
    public void tick(RecorderContext ctx) {
        ClientPacketListener conn = ctx.mc().getConnection();
        if (conn == null || ctx.mc().player == null) {
            return;
        }
        Map<AdvancementHolder, AdvancementProgress> progress =
                ClientReflect.advancementProgress(conn.getAdvancements());
        if (progress == null) {
            return; // reflection unavailable — feature disabled gracefully
        }

        long now = ctx.now();
        if (!seeded) {
            // Give the server a moment to send the initial advancement sync.
            if (now - sessionStartMs < 3000) {
                return;
            }
            for (Map.Entry<AdvancementHolder, AdvancementProgress> e : progress.entrySet()) {
                if (e.getValue().isDone()) {
                    seen.add(e.getKey().id().toString());
                }
            }
            seeded = true;
            lastPollMs = now;
            return;
        }

        if (now - lastPollMs < 5000) {
            return;
        }
        lastPollMs = now;

        for (Map.Entry<AdvancementHolder, AdvancementProgress> e : progress.entrySet()) {
            if (!e.getValue().isDone()) {
                continue;
            }
            AdvancementHolder holder = e.getKey();
            String id = holder.id().toString();
            if (!seen.add(id)) {
                continue; // already recorded
            }
            Optional<DisplayInfo> display = holder.value().display();
            if (display.isEmpty()) {
                continue; // recipe/no-display advancement
            }
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("adv", id);
            payload.put("title", display.get().getTitle().getString());
            payload.put("frame", display.get().getType().getSerializedName());
            ctx.emit(EventType.ADVANCEMENT, payload);
        }
    }
}
