package dev.nokhxyr.wherewasi;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Client config, backed by NeoForge's {@link ModConfigSpec}. Registered as a
 * {@code CLIENT} config in the mod constructor, so it lives in
 * {@code config/wherewasi-client.toml}.
 */
public final class WhereWasIConfig {

    public static final ModConfigSpec SPEC;
    public static final WhereWasIConfig CONFIG;

    static {
        Pair<WhereWasIConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(WhereWasIConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    public enum HudCorner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public final ModConfigSpec.IntValue positionSampleSeconds;
    public final ModConfigSpec.IntValue statsPollSeconds;
    public final ModConfigSpec.IntValue inventoryPollSeconds;
    public final ModConfigSpec.IntValue bulkAcquireThreshold;
    public final ModConfigSpec.IntValue zoneThresholdMinutes;
    public final ModConfigSpec.BooleanValue briefingEnabled;
    public final ModConfigSpec.IntValue briefingMinHoursSinceLast;
    public final ModConfigSpec.IntValue briefingDelaySeconds;
    public final ModConfigSpec.BooleanValue hudPinnedNote;
    public final ModConfigSpec.BooleanValue hudGuide;
    public final ModConfigSpec.EnumValue<HudCorner> hudCorner;
    public final ModConfigSpec.BooleanValue guideAutoClear;
    public final ModConfigSpec.IntValue guideArrivalRadius;
    public final ModConfigSpec.BooleanValue verboseZones;

    private WhereWasIConfig(ModConfigSpec.Builder b) {
        b.comment("Where Was I? — automatic play journal & resume briefing (client-side).");

        b.push("capture");
        positionSampleSeconds = b.comment("Seconds between position / zone-time samples.")
                .defineInRange("positionSampleSeconds", 15, 5, 600);
        statsPollSeconds = b.comment("Seconds between silent vanilla-statistics requests (the exact source of truth).")
                .defineInRange("statsPollSeconds", 120, 30, 1800);
        inventoryPollSeconds = b.comment("Seconds between inventory diffs (first-acquisition detection).")
                .defineInRange("inventoryPollSeconds", 10, 3, 120);
        bulkAcquireThreshold = b.comment("Items gained at once before logging a bulk acquisition.")
                .defineInRange("bulkAcquireThreshold", 64, 8, 2000);
        b.pop();

        b.push("zones");
        zoneThresholdMinutes = b.comment("Minutes accumulated in a 64x64 cell before WhereWasI proposes naming a zone.")
                .defineInRange("zoneThresholdMinutes", 20, 1, 600);
        b.pop();

        b.push("briefing");
        briefingEnabled = b.comment("Show the resume briefing after joining a world.")
                .define("briefingEnabled", true);
        briefingMinHoursSinceLast = b.comment("Only auto-show the briefing if the previous session ended at least this many hours ago.")
                .defineInRange("briefingMinHoursSinceLast", 6, 0, 240);
        briefingDelaySeconds = b.comment("Delay after joining before the briefing appears.")
                .defineInRange("briefingDelaySeconds", 3, 0, 30);
        b.pop();

        b.push("hud");
        hudPinnedNote = b.comment("Draw the pinned note on the HUD.")
                .define("hudPinnedNote", true);
        hudGuide = b.comment("Draw the guide arrow on the HUD when a target is active.")
                .define("hudGuide", true);
        hudCorner = b.comment("Which screen corner the HUD widgets attach to.")
                .defineEnum("hudCorner", HudCorner.TOP_LEFT);
        guideAutoClear = b.comment("Automatically hide the guide arrow once you reach the target.")
                .define("guideAutoClear", true);
        guideArrivalRadius = b.comment("How close (blocks) counts as 'arrived' for auto-hiding the guide.")
                .defineInRange("guideArrivalRadius", 8, 2, 64);
        b.pop();

        b.push("logging");
        verboseZones = b.comment("Log more inside your named zones (activity summaries, lower thresholds) and keep it light outside. All checks are interval-gated, so it stays cheap.")
                .define("verboseZones", true);
        b.pop();
    }
}
