package dev.nokhxyr.wherewasi.record;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import dev.nokhxyr.wherewasi.model.EventType;

/**
 * Emits a one-off {@link EventType#BIOME_ENTER} the first time the player stands
 * in a biome this world has never journalled — an exploration milestone. Backed by
 * the same persisted "discovered" set the {@link InventoryDiffer} uses, with a
 * {@code biome:} key prefix so it never collides with discovered item ids.
 */
public final class BiomeWatcher implements Sampler {

    private static final long CHECK_MS = 2000L;
    private static final String PREFIX = "biome:";

    private long lastCheckMs;
    private Set<String> discovered;
    private String lastKey;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        lastCheckMs = 0L;
        lastKey = null;
        discovered = ctx.recorder().discovered();
    }

    @Override
    public void tick(RecorderContext ctx) {
        LocalPlayer p = ctx.mc().player;
        if (p == null || discovered == null) {
            return;
        }
        long now = ctx.now();
        if (now - lastCheckMs < CHECK_MS) {
            return;
        }
        lastCheckMs = now;

        Holder<Biome> holder = p.level().getBiome(p.blockPosition());
        Optional<ResourceKey<Biome>> key = holder.unwrapKey();
        if (key.isEmpty()) {
            return;
        }
        String id = key.get().location().toString();
        if (id.equals(lastKey)) {
            return; // still in the same biome — nothing to do
        }
        lastKey = id;

        String discoveredKey = PREFIX + id;
        if (discovered.add(discoveredKey)) {
            ctx.recorder().saveDiscovered();
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("biome", id);
            ctx.emit(EventType.BIOME_ENTER, payload);
        }
    }
}
