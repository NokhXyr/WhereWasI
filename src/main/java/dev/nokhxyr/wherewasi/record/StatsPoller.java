package dev.nokhxyr.wherewasi.record;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import dev.nokhxyr.wherewasi.WhereWasIConfig;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.model.Zone;

/**
 * The heart of accurate capture. Periodically sends the same {@code REQUEST_STATS}
 * packet the vanilla Statistics screen uses, then reads the client
 * {@link StatsCounter} it populates. Because these are the server's authoritative
 * numbers, diffing them works identically on singleplayer, vanilla servers and
 * modded servers.
 *
 * <p>A baseline is captured a few seconds after joining (the response's round-trip)
 * so session aggregates are measured from "now", not from the player's lifetime
 * totals. Per-session firsts (first diamond mined, first netherite pickaxe crafted)
 * become milestone events.
 */
public final class StatsPoller implements Sampler {

    private static final Set<ResourceLocation> NOTABLE_BLOCKS = Set.of(
            rl("diamond_ore"), rl("deepslate_diamond_ore"), rl("ancient_debris"),
            rl("emerald_ore"), rl("deepslate_emerald_ore"));

    private static final Set<ResourceLocation> NOTABLE_ITEMS = Set.of(
            rl("diamond_pickaxe"), rl("diamond_sword"), rl("netherite_pickaxe"), rl("netherite_sword"),
            rl("netherite_ingot"), rl("beacon"), rl("enchanting_table"), rl("ender_chest"),
            rl("shield"), rl("brewing_stand"), rl("anvil"), rl("conduit"));

    private static final List<ResourceLocation> DISTANCE_STATS = List.of(
            Stats.WALK_ONE_CM, Stats.SPRINT_ONE_CM, Stats.CROUCH_ONE_CM, Stats.SWIM_ONE_CM, Stats.AVIATE_ONE_CM);

    private long sessionStartMs;
    private long lastPollMs;
    private boolean baselineCaptured;

    private long baselineMined;
    private long baselineDistanceCm;
    private int baselineMobKills;
    private int baselineDeaths;
    private final Map<String, Integer> baselineNotables = new HashMap<>();
    private final Set<String> firedNotables = new HashSet<>();
    private long prevPollMined;
    private int prevPollKills;
    private long nextInterval;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        sessionStartMs = ctx.now();
        lastPollMs = ctx.now();
        baselineCaptured = false;
        baselineNotables.clear();
        firedNotables.clear();
        requestStats(ctx); // warm the data so the baseline is real
    }

    @Override
    public void tick(RecorderContext ctx) {
        LocalPlayer p = ctx.mc().player;
        if (p == null) {
            return;
        }
        long now = ctx.now();

        if (!baselineCaptured) {
            if (now - sessionStartMs >= 4000) {
                captureBaseline(p.getStats());
                baselineCaptured = true;
                lastPollMs = now;
                nextInterval = WhereWasIConfig.CONFIG.statsPollSeconds.get() * 1000L;
            }
            return;
        }

        if (now - lastPollMs < nextInterval) {
            return;
        }
        lastPollMs = now;
        long base = WhereWasIConfig.CONFIG.statsPollSeconds.get() * 1000L;
        Zone zone = ctx.currentZone(); // one cheap zone check per poll, never per tick
        boolean verbose = zone != null && WhereWasIConfig.CONFIG.verboseZones.get();
        poll(ctx, p.getStats(), verbose);
        requestStats(ctx); // refresh for the next cycle
        // Finer cadence while inside a named zone, lighter outside.
        nextInterval = verbose ? Math.max(30_000L, base / 2L) : base;
    }

    private void captureBaseline(StatsCounter s) {
        baselineMined = totalMined(s);
        baselineMobKills = s.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        baselineDeaths = s.getValue(Stats.CUSTOM.get(Stats.DEATHS));
        baselineDistanceCm = totalDistanceCm(s);
        prevPollMined = baselineMined;
        prevPollKills = baselineMobKills;
        for (ResourceLocation b : NOTABLE_BLOCKS) {
            baselineNotables.put("block:" + b, minedCount(s, b));
        }
        for (ResourceLocation i : NOTABLE_ITEMS) {
            baselineNotables.put("item:" + i, craftedCount(s, i));
        }
    }

    private void poll(RecorderContext ctx, StatsCounter s, boolean verbose) {
        long totalMinedNow = totalMined(s);
        int mobKillsNow = s.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        long distNow = totalDistanceCm(s);

        int mined = (int) Math.max(0L, totalMinedNow - baselineMined);
        int mobKills = Math.max(0, mobKillsNow - baselineMobKills);
        int deaths = Math.max(0, s.getValue(Stats.CUSTOM.get(Stats.DEATHS)) - baselineDeaths);
        long distCm = Math.max(0L, distNow - baselineDistanceCm);
        ctx.recorder().updateSessionStats(mined, mobKills, deaths, distCm);

        for (ResourceLocation b : NOTABLE_BLOCKS) {
            String key = "block:" + b;
            if (firedNotables.contains(key)) {
                continue;
            }
            if (baselineNotables.getOrDefault(key, 0) == 0 && minedCount(s, b) > 0) {
                firedNotables.add(key);
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("block", b.toString());
                ctx.emit(EventType.MINE_MILESTONE, payload);
            }
        }
        for (ResourceLocation i : NOTABLE_ITEMS) {
            String key = "item:" + i;
            if (firedNotables.contains(key)) {
                continue;
            }
            if (baselineNotables.getOrDefault(key, 0) == 0 && craftedCount(s, i) > 0) {
                firedNotables.add(key);
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("item", i.toString());
                ctx.emit(EventType.FIRST_ACQUIRE, payload);
            }
        }

        // Verbose in-zone summary: what happened here since the last poll.
        if (verbose) {
            int minedDelta = (int) Math.max(0L, totalMinedNow - prevPollMined);
            int killDelta = Math.max(0, mobKillsNow - prevPollKills);
            if (minedDelta > 0 || killDelta > 0) {
                Map<String, String> activity = new LinkedHashMap<>();
                activity.put("mined", Integer.toString(minedDelta));
                activity.put("kills", Integer.toString(killDelta));
                ctx.emit(EventType.ZONE_ACTIVITY, activity);
            }
        }
        prevPollMined = totalMinedNow;
        prevPollKills = mobKillsNow;
    }

    private void requestStats(RecorderContext ctx) {
        ClientPacketListener conn = ctx.mc().getConnection();
        if (conn != null) {
            try {
                conn.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
            } catch (Exception ignored) {
                // not connected yet / transient — try again next cycle
            }
        }
    }

    private static long totalMined(StatsCounter s) {
        long total = 0L;
        for (Block b : BuiltInRegistries.BLOCK) {
            total += s.getValue(Stats.BLOCK_MINED.get(b));
        }
        return total;
    }

    private static long totalDistanceCm(StatsCounter s) {
        long total = 0L;
        for (ResourceLocation stat : DISTANCE_STATS) {
            total += s.getValue(Stats.CUSTOM.get(stat));
        }
        return total;
    }

    private static int minedCount(StatsCounter s, ResourceLocation id) {
        Block b = BuiltInRegistries.BLOCK.get(id);
        return s.getValue(Stats.BLOCK_MINED.get(b));
    }

    private static int craftedCount(StatsCounter s, ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        return s.getValue(Stats.ITEM_CRAFTED.get(item));
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
