package dev.nokhxyr.wherewasi.record;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import dev.nokhxyr.wherewasi.WhereWasIConfig;
import dev.nokhxyr.wherewasi.model.EventType;

/**
 * Diffs the player's inventory on an interval to catch two kinds of milestone:
 * the first time an item is ever acquired in this world (a progression marker),
 * and large single-step gains (a configurable threshold). "First ever" is backed
 * by a persisted discovered-items set; on a world WhereWasI has never tracked
 * before, the starting inventory is seeded silently so it doesn't all fire at once.
 */
public final class InventoryDiffer implements Sampler {

    private long lastPollMs;
    private final Map<String, Integer> prevCounts = new HashMap<>();
    private Set<String> discovered;
    private boolean discoveredDirty;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        lastPollMs = ctx.now();
        prevCounts.clear();
        discovered = ctx.recorder().discovered();

        Map<String, Integer> current = snapshot(ctx);
        prevCounts.putAll(current);

        if (ctx.recorder().isFirstTimeWorld()) {
            discovered.addAll(current.keySet());
            discoveredDirty = !current.isEmpty();
        }
        if (discoveredDirty) {
            ctx.recorder().saveDiscovered();
            discoveredDirty = false;
        }
    }

    @Override
    public void tick(RecorderContext ctx) {
        if (ctx.mc().player == null) {
            return;
        }
        long now = ctx.now();
        long interval = WhereWasIConfig.CONFIG.inventoryPollSeconds.get() * 1000L;
        if (now - lastPollMs < interval) {
            return;
        }
        lastPollMs = now;

        Map<String, Integer> current = snapshot(ctx);
        int bulk = WhereWasIConfig.CONFIG.bulkAcquireThreshold.get();

        for (Map.Entry<String, Integer> entry : current.entrySet()) {
            String id = entry.getKey();
            int count = entry.getValue();
            int prev = prevCounts.getOrDefault(id, 0);

            if (!discovered.contains(id)) {
                discovered.add(id);
                discoveredDirty = true;
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("item", id);
                ctx.emit(EventType.FIRST_ACQUIRE, payload);
            } else if (count - prev >= bulk) {
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("item", id);
                payload.put("count", Integer.toString(count - prev));
                ctx.emit(EventType.BULK_ACQUIRE, payload);
            }
        }

        prevCounts.clear();
        prevCounts.putAll(current);
        if (discoveredDirty) {
            ctx.recorder().saveDiscovered();
            discoveredDirty = false;
        }
    }

    private Map<String, Integer> snapshot(RecorderContext ctx) {
        Map<String, Integer> counts = new HashMap<>();
        LocalPlayer p = ctx.mc().player;
        if (p == null) {
            return counts;
        }
        Inventory inv = p.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            counts.merge(id.toString(), stack.getCount(), Integer::sum);
        }
        return counts;
    }
}
