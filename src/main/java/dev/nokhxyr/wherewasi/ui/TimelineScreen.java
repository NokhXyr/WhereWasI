package dev.nokhxyr.wherewasi.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.model.Session;
import dev.nokhxyr.wherewasi.model.Zone;
import dev.nokhxyr.wherewasi.record.ActivityRecorder;
import dev.nokhxyr.wherewasi.storage.JournalStorage;

/**
 * The journal timeline: your play history grouped into collapsible sessions, newest
 * first. Each session header shows its span, duration, main zone and what you did;
 * expanding it reveals a vertical rail of that session's events in order. Clicking
 * an event opens its coordinates and a "Guide" button that lights the HUD arrow.
 * Type and zone filters cycle across the whole history.
 */
public final class TimelineScreen extends Screen {

    private static final int DETAIL_H = 26;

    /** One play session and the events that fall inside its window (chronological). */
    private record Group(long start, long end, boolean live, Session session, List<ActivityEvent> events) {
    }

    private final ScrollList list = new ScrollList();
    private List<Group> groups = List.of();

    private final Set<Long> expandedGroups = new HashSet<>();
    private final Set<Long> expandedEvents = new HashSet<>();

    private int typeIdx; // 0 = all, else EventType.values()[typeIdx-1]
    private int zoneIdx; // 0 = all, else zones.get(zoneIdx-1)
    private List<Zone> zones = List.of();

    private Button typeButton;
    private Button zoneButton;

    public TimelineScreen() {
        super(Component.translatable("wherewasi.timeline.title"));
    }

    @Override
    protected void init() {
        load();

        list.bounds(20, 48, width - 40, height - 92);

        typeButton = Button.builder(typeLabel(), b -> {
            typeIdx = (typeIdx + 1) % (EventType.values().length + 1);
            typeButton.setMessage(typeLabel());
            rebuild();
        }).bounds(20, 22, 150, 20).build();
        addRenderableWidget(typeButton);

        zoneButton = Button.builder(zoneLabel(), b -> {
            if (!zones.isEmpty()) {
                zoneIdx = (zoneIdx + 1) % (zones.size() + 1);
                zoneButton.setMessage(zoneLabel());
                rebuild();
            }
        }).bounds(176, 22, 150, 20).build();
        addRenderableWidget(zoneButton);

        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.zones"),
                        b -> minecraft.setScreen(new ZonesScreen()))
                .bounds(width - 20 - 100, 22, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.close"), b -> onClose())
                .bounds(width / 2 - 50, height - 28, 100, 20).build());

        rebuild();
    }

    // ---- data --------------------------------------------------------------

    private void load() {
        ActivityRecorder rec = ClientState.recorder();
        JournalStorage storage = rec.storage();
        List<ActivityEvent> events = storage != null ? new ArrayList<>(storage.loadEvents()) : new ArrayList<>();
        events.sort(Comparator.comparingLong(ActivityEvent::time));
        List<Session> sessions = storage != null ? storage.loadSessions() : List.of();
        zones = new ArrayList<>(rec.zones().zones());

        groups = buildGroups(events, sessions, rec.sessionActive());
        if (!groups.isEmpty()) {
            expandedGroups.add(groups.get(0).start()); // most recent session open by default
        }
    }

    private static List<Group> buildGroups(List<ActivityEvent> events, List<Session> sessions, boolean live) {
        boolean[] taken = new boolean[events.size()];
        List<Group> result = new ArrayList<>();
        for (Session s : sessions) {
            long from = s.startEpochMs();
            long to = s.endEpochMs() + 2000L;
            List<ActivityEvent> evs = new ArrayList<>();
            for (int i = 0; i < events.size(); i++) {
                ActivityEvent e = events.get(i);
                if (!taken[i] && e.time() >= from && e.time() <= to) {
                    evs.add(e);
                    taken[i] = true;
                }
            }
            result.add(new Group(s.startEpochMs(), s.endEpochMs(), false, s, evs));
        }
        // Anything not inside a finished session is the current (or a crashed) session.
        List<ActivityEvent> leftover = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            if (!taken[i]) {
                leftover.add(events.get(i));
            }
        }
        if (!leftover.isEmpty()) {
            long from = leftover.get(0).time();
            long to = leftover.get(leftover.size() - 1).time();
            result.add(new Group(from, live ? System.currentTimeMillis() : to, live, null, leftover));
        }
        result.sort(Comparator.comparingLong(Group::start).reversed());
        return result;
    }

    private void rebuild() {
        EventType typeFilter = typeIdx == 0 ? null : EventType.values()[typeIdx - 1];
        String zoneFilter = zoneIdx == 0 || zones.isEmpty() ? null : zones.get(zoneIdx - 1).id();
        boolean filtering = typeFilter != null || zoneFilter != null;

        List<ScrollList.Row> rows = new ArrayList<>();
        boolean any = false;
        for (Group g : groups) {
            List<ActivityEvent> evs = visibleEvents(g, typeFilter, zoneFilter);
            if (filtering && evs.isEmpty()) {
                continue;
            }
            any = true;
            rows.add(groupHeader(g, evs.size()));
            if (expandedGroups.contains(g.start())) {
                for (ActivityEvent e : evs) {
                    rows.add(eventRow(e, zoneFilter != null));
                }
            }
        }
        if (!any) {
            rows.add(emptyRow());
        }
        list.setRows(rows);
    }

    private List<ActivityEvent> visibleEvents(Group g, EventType typeFilter, String zoneFilter) {
        List<ActivityEvent> out = new ArrayList<>();
        for (ActivityEvent e : g.events()) {
            EventType t = e.type();
            if (typeFilter == null) {
                if (t == EventType.SESSION_START || t == EventType.SESSION_END) {
                    continue; // the session header already represents these
                }
            } else if (t != typeFilter) {
                continue;
            }
            if (zoneFilter != null && !zoneFilter.equals(e.zoneId())) {
                continue;
            }
            out.add(e);
        }
        return out;
    }

    // ---- rows --------------------------------------------------------------

    private ScrollList.Row groupHeader(Group g, int visibleCount) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 30;
            }

            @Override
            public void render(GuiGraphics gg, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                boolean open = expandedGroups.contains(g.start());
                if (hovered) {
                    gg.fill(x, y, x + width, y + 30, 0x18FFFFFF);
                }
                gg.drawString(font, Component.literal(open ? "▼" : "▶"), x + 2, y + 3, UiText.COL_ACCENT);

                gg.drawString(font, headerTitle(g), x + 16, y + 3, UiText.COL_TITLE);
                Component sub = headerSubtitle(g, visibleCount);
                gg.enableScissor(x + 16, y + 14, x + width - 6, y + 26);
                gg.drawString(font, sub, x + 16, y + 16, UiText.COL_DIM);
                gg.disableScissor();

                gg.fill(x, y + 29, x + width, y + 30, UiText.COL_PANEL_LIGHT);
            }

            @Override
            public boolean click(double mouseX, double mouseY, int x, int y, int width) {
                if (open(g)) {
                    expandedGroups.remove(g.start());
                } else {
                    expandedGroups.add(g.start());
                }
                rebuild();
                return true;
            }
        };
    }

    private boolean open(Group g) {
        return expandedGroups.contains(g.start());
    }

    private Component headerTitle(Group g) {
        var line = Component.empty().append(UiText.dayHeading(g.start()));
        if (g.live()) {
            line.append(" · ").append(Component.translatable("wherewasi.timeline.live_since", UiText.clock(g.start())));
        } else {
            line.append(" · ").append(UiText.clock(g.start()) + "–" + UiText.clock(g.end()));
            line.append(" · ").append(UiText.duration(g.end() - g.start()));
        }
        return line;
    }

    private Component headerSubtitle(Group g, int visibleCount) {
        if (g.session() == null) { // current session, or an unfinished/crashed one with no summary
            Component count = Component.translatable("wherewasi.timeline.event_count", visibleCount);
            if (g.live()) {
                return Component.translatable("wherewasi.timeline.in_progress").copy().append(" · ").append(count);
            }
            return count;
        }
        var line = Component.empty();
        Component headline = UiText.sessionHeadline(g.session());
        boolean hasHeadline = !headline.getString().isEmpty();
        if (hasHeadline) {
            line.append(headline);
        }
        String zoneName = zoneName(g.session().mainZoneId());
        if (zoneName != null) {
            if (hasHeadline) {
                line.append(" · ");
            }
            line.append(zoneName);
        }
        if (!hasHeadline && zoneName == null) {
            line.append(Component.translatable("wherewasi.timeline.event_count", visibleCount));
        }
        return line;
    }

    private ScrollList.Row eventRow(ActivityEvent e, boolean zoneFiltered) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 20 + (expandedEvents.contains(e.time()) ? DETAIL_H : 0);
            }

            @Override
            public void render(GuiGraphics gg, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                boolean expanded = expandedEvents.contains(e.time());
                int rowH = height();
                int railX = x + 40;
                int iconX = x + 48;
                int textX = x + 70;

                boolean overTop = hovered && mouseY < y + 20;
                if (overTop) {
                    gg.fill(x, y, x + width, y + 20, 0x14FFFFFF);
                }

                // Continuous rail with a node at the event's moment.
                gg.fill(railX, y, railX + 1, y + rowH, UiText.COL_PANEL_LIGHT);
                int dot = e.type() == EventType.DEATH ? UiText.COL_WARN : UiText.COL_ACCENT;
                gg.fill(railX - 2, y + 7, railX + 3, y + 12, dot);

                gg.drawString(font, UiText.clock(e.time()), x + 2, y + 6, UiText.COL_DIM);
                gg.renderItem(UiText.iconFor(e), iconX, y + 2);

                int chevronX = x + width - 9;
                gg.drawString(font, Component.literal(expanded ? "▾" : "▸"), chevronX, y + 6, UiText.COL_DIM);

                int rightBound = chevronX - 4;
                if (!zoneFiltered) {
                    Zone zone = ClientState.recorder().zones().zoneById(e.zoneId());
                    if (zone != null) {
                        Component zn = Component.literal(zone.name());
                        int zx = chevronX - 6 - font.width(zn);
                        gg.drawString(font, zn, zx, y + 6, UiText.COL_DIM);
                        rightBound = zx - 4;
                    }
                }

                if (rightBound > textX) {
                    gg.enableScissor(textX, y, rightBound, y + 20);
                    gg.drawString(font, UiText.describe(e), textX, y + 6, UiText.COL_TEXT);
                    gg.disableScissor();
                }

                if (expanded) {
                    int y0 = y + 20;
                    Component coords = Component.literal(e.x() + ", " + e.y() + ", " + e.z() + "  ")
                            .copy().append(UiText.dimensionName(e.dim()));
                    gg.drawString(font, coords, textX, y0 + 6, UiText.COL_DIM);
                    drawGuideButton(gg, x, width, y0, mouseX, mouseY);
                }
            }

            @Override
            public boolean click(double mouseX, double mouseY, int x, int y, int width) {
                if (expandedEvents.contains(e.time()) && inGuideButton(mouseX, mouseY, x, width, y + 20)) {
                    guideTo(e);
                    return true;
                }
                if (expandedEvents.contains(e.time())) {
                    expandedEvents.remove(e.time());
                } else {
                    expandedEvents.add(e.time());
                }
                rebuild();
                return true;
            }
        };
    }

    private void drawGuideButton(GuiGraphics gg, int x, int width, int y0, int mouseX, int mouseY) {
        int bw = 56;
        int bh = 16;
        int bx = x + width - 8 - bw;
        int by = y0 + 2;
        boolean hot = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        gg.fill(bx, by, bx + bw, by + bh, hot ? 0x40FFFFFF : UiText.COL_PANEL_LIGHT);
        Component gl = Component.translatable("wherewasi.btn.guide");
        gg.drawString(font, gl, bx + (bw - font.width(gl)) / 2, by + 4, UiText.COL_ACCENT);
    }

    private boolean inGuideButton(double mouseX, double mouseY, int x, int width, int y0) {
        int bw = 56;
        int bh = 16;
        int bx = x + width - 8 - bw;
        int by = y0 + 2;
        return mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
    }

    private void guideTo(ActivityEvent e) {
        String label = UiText.describe(e).getString();
        if (label.length() > 28) {
            label = label.substring(0, 27) + "…";
        }
        ClientState.setGuideTarget(new GuideTarget(e.dim(), e.x(), e.y(), e.z(), label));
        Toasts.show(Component.translatable("wherewasi.toast.guide.title"),
                Component.translatable("wherewasi.toast.guide.msg", label));
    }

    private ScrollList.Row emptyRow() {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 24;
            }

            @Override
            public void render(GuiGraphics gg, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                gg.drawString(font, Component.translatable("wherewasi.timeline.empty"), x + 4, y + 8, UiText.COL_DIM);
            }
        };
    }

    // ---- helpers -----------------------------------------------------------

    private String zoneName(String zoneId) {
        if (zoneId == null) {
            return null;
        }
        Zone z = ClientState.recorder().zones().zoneById(zoneId);
        return z == null ? null : z.name();
    }

    private Component typeLabel() {
        Component value = typeIdx == 0
                ? Component.translatable("wherewasi.filter.all")
                : Component.translatable("wherewasi.type." + EventType.values()[typeIdx - 1].name().toLowerCase());
        return Component.translatable("wherewasi.timeline.filter_type", value);
    }

    private Component zoneLabel() {
        Component value = zoneIdx == 0 || zones.isEmpty()
                ? Component.translatable("wherewasi.filter.all")
                : Component.literal(zones.get(zoneIdx - 1).name());
        return Component.translatable("wherewasi.timeline.filter_zone", value);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawString(font, title, 20, 8, UiText.COL_TITLE);
        list.render(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (list.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (list.mouseClicked(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
