package dev.nokhxyr.wherewasi.record;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import dev.nokhxyr.wherewasi.model.EventType;

/**
 * Detects the rising edge of the client player dying and records where it
 * happened (current position/dimension via {@link RecorderContext#emit}) and the
 * cause, read from the client combat tracker. No server hook required.
 */
public final class DeathWatcher implements Sampler {

    private boolean wasDead;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        LocalPlayer p = ctx.mc().player;
        wasDead = p != null && p.isDeadOrDying();
    }

    @Override
    public void tick(RecorderContext ctx) {
        LocalPlayer p = ctx.mc().player;
        if (p == null) {
            wasDead = false;
            return;
        }
        boolean dead = p.isDeadOrDying();
        if (dead && !wasDead) {
            Map<String, String> payload = new LinkedHashMap<>();
            try {
                Component msg = p.getCombatTracker().getDeathMessage();
                String s = msg == null ? null : msg.getString();
                if (s != null && !s.isBlank()) {
                    payload.put("cause", s);
                }
            } catch (Exception ignored) {
                // combat tracker occasionally has no message client-side
            }
            ctx.emit(EventType.DEATH, payload);
        }
        wasDead = dead;
    }
}
