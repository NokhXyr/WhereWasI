package dev.nokhxyr.wherewasi.record;

/**
 * A pluggable capture source. WhereWasI calls {@link #tick} every client tick while
 * a session is active; implementations gate their real work on time intervals
 * (via {@link RecorderContext#now()}) so per-tick cost is a cheap timestamp check
 * with no allocation.
 */
public interface Sampler {

    default void onSessionStart(RecorderContext ctx) {
    }

    void tick(RecorderContext ctx);

    default void onSessionEnd(RecorderContext ctx) {
    }
}
