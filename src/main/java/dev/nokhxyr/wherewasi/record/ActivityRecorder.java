package dev.nokhxyr.wherewasi.record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import dev.nokhxyr.wherewasi.WhereWasI;
import dev.nokhxyr.wherewasi.WhereWasIConfig;
import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.model.Note;
import dev.nokhxyr.wherewasi.model.Session;
import dev.nokhxyr.wherewasi.model.WorldRef;
import dev.nokhxyr.wherewasi.storage.JournalPaths;
import dev.nokhxyr.wherewasi.storage.JournalStorage;
import dev.nokhxyr.wherewasi.ui.Briefing;
import dev.nokhxyr.wherewasi.ui.BriefingScreen;
import dev.nokhxyr.wherewasi.ui.Toasts;
import dev.nokhxyr.wherewasi.zones.ZoneTracker;

/**
 * The client-side session engine: it owns the active world's storage, zone
 * tracker and notes, drives the {@link Sampler}s once per tick, and produces a
 * recomputed {@link Session} summary when the session ends. There is exactly one
 * instance (see {@code ClientState}).
 */
public final class ActivityRecorder {

    private final List<Sampler> samplers = List.of(
            new PositionSampler(),
            new StatsPoller(),
            new InventoryDiffer(),
            new AdvancementWatcher(),
            new DeathWatcher());

    private final ZoneTracker zones = new ZoneTracker();

    private RecorderContext ctx;
    private boolean sessionActive;

    private JournalStorage storage;
    private List<Note> notes = new ArrayList<>();
    private Set<String> discovered;
    private boolean firstTimeWorld;

    // Live session state -----------------------------------------------------
    private String sessionId;
    private String worldId;
    private String worldName;
    private long startMs;
    private String lastDim = "minecraft:overworld";
    private int lastX, lastY, lastZ;
    private int blocksMined, mobsKilled, deaths;
    private long distanceCm;
    private int eventCount;
    private final Map<String, Long> sessionZoneDwell = new LinkedHashMap<>();

    // Briefing scheduling ----------------------------------------------------
    private boolean briefingPending;
    private long briefingDueMs;

    // ---- tick loop ---------------------------------------------------------

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            if (sessionActive) {
                endSession();
            }
            return;
        }
        if (!sessionActive) {
            startSession(mc);
        }
        for (Sampler s : samplers) {
            try {
                s.tick(ctx);
            } catch (Exception e) {
                WhereWasI.LOGGER.warn("WhereWasI: sampler {} failed", s.getClass().getSimpleName(), e);
            }
        }
        tickBriefing(mc);
    }

    public void onLoggingOut() {
        if (sessionActive) {
            endSession();
        }
    }

    // ---- session lifecycle -------------------------------------------------

    private void startSession(Minecraft mc) {
        LocalPlayer player = mc.player;
        WorldRef world = WorldRef.current(mc);

        storage = new JournalStorage(JournalPaths.worldDir(world.id()));
        zones.load(storage);
        notes = storage.loadNotes();
        discovered = storage.loadDiscovered();
        List<Session> previous = storage.loadSessions();
        firstTimeWorld = previous.isEmpty() && discovered.isEmpty();

        worldId = world.id();
        worldName = world.name();
        sessionId = Long.toString(System.currentTimeMillis());
        startMs = System.currentTimeMillis();
        blocksMined = mobsKilled = deaths = 0;
        distanceCm = 0L;
        eventCount = 0;
        sessionZoneDwell.clear();
        if (player != null) {
            BlockPos pos = player.blockPosition();
            lastDim = player.level().dimension().location().toString();
            lastX = pos.getX();
            lastY = pos.getY();
            lastZ = pos.getZ();
        }

        ctx = new RecorderContext(mc, this);
        sessionActive = true;

        prepareBriefing(previous);

        for (Sampler s : samplers) {
            try {
                s.onSessionStart(ctx);
            } catch (Exception e) {
                WhereWasI.LOGGER.warn("WhereWasI: sampler {} onSessionStart failed", s.getClass().getSimpleName(), e);
            }
        }

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("world", worldName);
        ctx.emit(EventType.SESSION_START, payload);
        WhereWasI.LOGGER.info("WhereWasI: session started for world '{}' ({})", worldName, worldId);
    }

    private void endSession() {
        sessionActive = false;
        try {
            for (Sampler s : samplers) {
                try {
                    s.onSessionEnd(ctx);
                } catch (Exception e) {
                    WhereWasI.LOGGER.warn("WhereWasI: sampler onSessionEnd failed", e);
                }
            }
            String mainZoneId = mainZone();
            long end = System.currentTimeMillis();

            // Built directly (not via ctx.emit) because the player is usually already gone.
            record(new ActivityEvent(end, EventType.SESSION_END, lastDim, lastX, lastY, lastZ,
                    mainZoneId, EventType.SESSION_END.baseImportance(), Map.of()));

            Session summary = new Session(sessionId, worldId, worldName, startMs, end,
                    lastDim, lastX, lastY, lastZ, mainZoneId,
                    blocksMined, mobsKilled, deaths, distanceCm, eventCount);
            storage.appendSession(summary);
            zones.save(storage);
            storage.saveNotes(notes);
            if (discovered != null) {
                storage.saveDiscovered(discovered);
            }
            storage.flush();
            storage.close();
            WhereWasI.LOGGER.info("WhereWasI: session ended ({} events, {} min)", eventCount,
                    summary.durationMs() / 60000);
        } catch (Exception e) {
            WhereWasI.LOGGER.warn("WhereWasI: endSession failed", e);
        }
        briefingPending = false;
    }

    private String mainZone() {
        String best = null;
        long bestMs = -1L;
        for (Map.Entry<String, Long> e : sessionZoneDwell.entrySet()) {
            if (e.getValue() > bestMs) {
                bestMs = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    // ---- event & state sink (called by samplers) ---------------------------

    void record(ActivityEvent e) {
        if (storage != null) {
            storage.appendEvent(e);
        }
        eventCount++;
    }

    void updateLastPosition(String dim, int x, int y, int z) {
        lastDim = dim;
        lastX = x;
        lastY = y;
        lastZ = z;
    }

    void updateSessionStats(int mined, int mobKills, int sessionDeaths, long distCm) {
        this.blocksMined = mined;
        this.mobsKilled = mobKills;
        this.deaths = sessionDeaths;
        this.distanceCm = distCm;
    }

    void addZoneDwell(String zoneId, long ms) {
        sessionZoneDwell.merge(zoneId, ms, Long::sum);
    }

    void onZoneCandidate(ZoneTracker.Candidate c) {
        Toasts.show(Component.translatable("wherewasi.toast.zone.title"),
                Component.translatable("wherewasi.toast.zone.msg", c.x(), c.z()));
        if (storage != null) {
            zones.save(storage);
        }
    }

    Set<String> discovered() {
        return discovered;
    }

    boolean isFirstTimeWorld() {
        return firstTimeWorld;
    }

    void saveDiscovered() {
        if (storage != null && discovered != null) {
            storage.saveDiscovered(discovered);
        }
    }

    // ---- notes -------------------------------------------------------------

    public Note createNote(String text, boolean pinned) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        String dim = lastDim;
        int x = lastX, y = lastY, z = lastZ;
        if (p != null) {
            BlockPos pos = p.blockPosition();
            dim = p.level().dimension().location().toString();
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
        }
        String zoneId = zones.zoneIdAt(dim, x, z);
        String id = "note_" + System.currentTimeMillis();
        if (pinned) {
            unpinAll();
        }
        Note note = new Note(id, System.currentTimeMillis(), dim, x, y, z, zoneId, text, pinned);
        notes.add(note);
        saveNotes();

        if (ctx != null && p != null) {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("text", text);
            payload.put("noteId", id);
            ctx.emit(EventType.NOTE, payload);
        }
        return note;
    }

    public void setPinned(String noteId) {
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            notes.set(i, n.withPinned(n.id().equals(noteId)));
        }
        saveNotes();
    }

    public void unpinAll() {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).pinned()) {
                notes.set(i, notes.get(i).withPinned(false));
            }
        }
        saveNotes();
    }

    public void deleteNote(String id) {
        notes.removeIf(n -> n.id().equals(id));
        saveNotes();
    }

    private void saveNotes() {
        if (storage != null) {
            storage.saveNotes(notes);
        }
    }

    public Note pinnedNote() {
        for (Note n : notes) {
            if (n.pinned()) {
                return n;
            }
        }
        return null;
    }

    // ---- briefing ----------------------------------------------------------

    private void prepareBriefing(List<Session> previous) {
        briefingPending = false;
        if (!WhereWasIConfig.CONFIG.briefingEnabled.get() || previous.isEmpty()) {
            return;
        }
        Session last = previous.get(previous.size() - 1);
        long hoursSince = (System.currentTimeMillis() - last.endEpochMs()) / 3_600_000L;
        if (hoursSince < WhereWasIConfig.CONFIG.briefingMinHoursSinceLast.get()) {
            return;
        }
        briefingDueMs = System.currentTimeMillis() + WhereWasIConfig.CONFIG.briefingDelaySeconds.get() * 1000L;
        briefingPending = true;
    }

    private void tickBriefing(Minecraft mc) {
        if (!briefingPending || System.currentTimeMillis() < briefingDueMs || mc.screen != null) {
            return;
        }
        briefingPending = false;
        openBriefing(false);
    }

    public void openBriefing(boolean force) {
        Minecraft mc = Minecraft.getInstance();
        Briefing briefing = Briefing.build(this);
        if (briefing == null) {
            if (force) {
                Toasts.show(Component.translatable("wherewasi.briefing.none.title"),
                        Component.translatable("wherewasi.briefing.none.msg"));
            }
            return;
        }
        mc.setScreen(new BriefingScreen(briefing));
    }

    // ---- accessors for UI --------------------------------------------------

    public ZoneTracker zones() {
        return zones;
    }

    public JournalStorage storage() {
        return storage;
    }

    public List<Note> notes() {
        return notes;
    }

    public boolean sessionActive() {
        return sessionActive;
    }

    public void saveZones() {
        if (storage != null) {
            zones.save(storage);
        }
    }
}
