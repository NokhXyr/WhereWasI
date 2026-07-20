package dev.nokhxyr.wherewasi.compat;

import dev.nokhxyr.wherewasi.model.ActivityEvent;

/**
 * Public extension point for add-ons that want to feed their own events into the
 * WhereWasI journal — for example an FTB&nbsp;Quests bridge that logs quest
 * completions, or a minimap bridge that logs waypoint creation.
 *
 * <p><b>Status: skeleton only.</b> The interface and its {@link Context} are the
 * stable, published contract. WhereWasI does not yet discover or tick third-party
 * detectors; a registration entry point (likely a {@code RegisterActivityDetectors}
 * event fired on the mod bus) will be added in a later version. It is exposed now
 * so add-on authors can compile against a fixed shape.
 *
 * <p>Implementations run on the client thread, once per session and then on every
 * client tick while a session is active. They should be cheap: gate real work on
 * {@link Context#now()} intervals rather than doing something every tick.
 */
public interface ActivityDetector {

    /**
     * A stable, unique id for this detector (e.g. {@code "ftbquests"}). Used for
     * logging and, in the future, for enabling/disabling detectors via config.
     */
    String id();

    /** Called once when a session starts, before the first {@link #tick}. */
    default void onSessionStart(Context ctx) {
    }

    /** Called every client tick while a session is active. Keep it cheap. */
    void tick(Context ctx);

    /** Called once when the session ends (logout / world close). */
    default void onSessionEnd(Context ctx) {
    }

    /**
     * What a detector is allowed to touch: the clock, the player's current
     * location, and a sink to emit finished {@link ActivityEvent}s. WhereWasI fills
     * in importance scoring and zone tagging itself where appropriate.
     */
    interface Context {

        /** Wall-clock time in epoch milliseconds. */
        long now();

        /** Current dimension id (e.g. {@code "minecraft:overworld"}), or {@code null} if unknown. */
        String dimension();

        int x();

        int y();

        int z();

        /** Append a fully-built event to the current session's journal. */
        void emit(ActivityEvent event);
    }
}
