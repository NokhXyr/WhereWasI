package dev.nokhxyr.wherewasi.model;

/**
 * The kinds of things WhereWasI records in a session journal, each with a base
 * importance. The final importance of an {@link ActivityEvent} is this base
 * plus contextual bonuses (see {@link Importance}); the briefing surfaces the
 * highest-scoring events.
 */
public enum EventType {
    SESSION_START(1),
    SESSION_END(1),
    SEGMENT(2),
    DIMENSION_CHANGE(3),
    BIOME_ENTER(3),
    DEATH(8),
    ADVANCEMENT(5),
    FIRST_ACQUIRE(4),
    BULK_ACQUIRE(3),
    MINE_MILESTONE(6),
    ZONE_NAMED(2),
    ZONE_ACTIVITY(2),
    NOTE(6);

    private final int baseImportance;

    EventType(int baseImportance) {
        this.baseImportance = baseImportance;
    }

    public int baseImportance() {
        return baseImportance;
    }
}
