package dev.nokhxyr.wherewasi.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.model.Note;
import dev.nokhxyr.wherewasi.model.Session;
import dev.nokhxyr.wherewasi.model.Zone;
import dev.nokhxyr.wherewasi.record.ActivityRecorder;
import dev.nokhxyr.wherewasi.storage.JournalStorage;

/**
 * Immutable data for the resume briefing: the previous session, its main zone,
 * its most significant events (top 5 by importance), its deaths, and the pinned
 * note. Built by reading back the previous session's window from storage.
 */
public record Briefing(
        Session last,
        Zone mainZone,
        List<ActivityEvent> topEvents,
        List<ActivityEvent> deaths,
        Note pinned
) {

    public static Briefing build(ActivityRecorder rec) {
        JournalStorage storage = rec.storage();
        if (storage == null) {
            return null;
        }
        List<Session> sessions = storage.loadSessions();
        if (sessions.isEmpty()) {
            return null;
        }
        Session last = sessions.get(sessions.size() - 1);

        long from = last.startEpochMs();
        long to = last.endEpochMs() + 2000L;
        List<ActivityEvent> deaths = new ArrayList<>();
        List<ActivityEvent> notable = new ArrayList<>();
        for (ActivityEvent e : storage.loadEvents()) {
            if (e.time() < from || e.time() > to) {
                continue;
            }
            if (e.type() == EventType.DEATH) {
                deaths.add(e);
            } else if (e.type() != EventType.SESSION_START && e.type() != EventType.SESSION_END) {
                notable.add(e);
            }
        }
        notable.sort(Comparator.comparingInt(ActivityEvent::importance).reversed()
                .thenComparing(Comparator.comparingLong(ActivityEvent::time).reversed()));
        List<ActivityEvent> top = notable.size() > 5 ? new ArrayList<>(notable.subList(0, 5)) : notable;

        Zone mainZone = rec.zones().zoneById(last.mainZoneId());
        return new Briefing(last, mainZone, top, deaths, rec.pinnedNote());
    }
}
