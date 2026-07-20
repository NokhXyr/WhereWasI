package dev.nokhxyr.wherewasi.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.EventType;
import dev.nokhxyr.wherewasi.model.Zone;
import dev.nokhxyr.wherewasi.record.ActivityRecorder;
import dev.nokhxyr.wherewasi.storage.JournalStorage;

/**
 * The journal timeline: every recorded event, newest first, grouped by day, with
 * item icons and cycling filters for event type and zone. Scrolls smoothly via
 * {@link ScrollList}.
 */
public final class TimelineScreen extends Screen {

    private final ScrollList list = new ScrollList();
    private List<ActivityEvent> allEvents = List.of();

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
        ActivityRecorder rec = ClientState.recorder();
        JournalStorage storage = rec.storage();
        allEvents = storage != null ? storage.loadEvents() : List.of();
        allEvents = new ArrayList<>(allEvents);
        allEvents.sort(Comparator.comparingLong(ActivityEvent::time).reversed());
        zones = new ArrayList<>(rec.zones().zones());

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

    private void rebuild() {
        EventType typeFilter = typeIdx == 0 ? null : EventType.values()[typeIdx - 1];
        String zoneFilter = zoneIdx == 0 ? null : zones.get(zoneIdx - 1).id();

        List<ActivityEvent> filtered = new ArrayList<>();
        for (ActivityEvent e : allEvents) {
            if (typeFilter != null && e.type() != typeFilter) {
                continue;
            }
            if (zoneFilter != null && !zoneFilter.equals(e.zoneId())) {
                continue;
            }
            filtered.add(e);
        }

        Map<String, List<ActivityEvent>> byDay = new LinkedHashMap<>();
        for (ActivityEvent e : filtered) {
            byDay.computeIfAbsent(UiText.dayLabel(e.time()), d -> new ArrayList<>()).add(e);
        }

        List<ScrollList.Row> rows = new ArrayList<>();
        if (filtered.isEmpty()) {
            rows.add(emptyRow());
        }
        for (Map.Entry<String, List<ActivityEvent>> day : byDay.entrySet()) {
            rows.add(dayHeader(day.getKey()));
            for (ActivityEvent e : day.getValue()) {
                rows.add(eventRow(e));
            }
        }
        list.setRows(rows);
    }

    private ScrollList.Row dayHeader(String label) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 16;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                g.fill(x, y + 13, x + width, y + 14, UiText.COL_PANEL_LIGHT);
                g.drawString(font, Component.literal(label), x + 2, y + 4, UiText.COL_ACCENT);
            }
        };
    }

    private ScrollList.Row eventRow(ActivityEvent e) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 20;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                if (hovered) {
                    g.fill(x, y, x + width, y + 20, 0x18FFFFFF);
                }
                g.drawString(font, UiText.clock(e.time()), x + 2, y + 6, UiText.COL_DIM);
                g.renderItem(UiText.iconFor(e), x + 34, y + 2);
                g.drawString(font, UiText.describe(e), x + 54, y + 6, UiText.COL_TEXT);
                Zone zone = ClientState.recorder().zones().zoneById(e.zoneId());
                if (zone != null) {
                    Component zn = Component.literal(zone.name());
                    g.drawString(font, zn, x + width - 8 - font.width(zn), y + 6, UiText.COL_DIM);
                }
            }
        };
    }

    private ScrollList.Row emptyRow() {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 24;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                g.drawString(font, Component.translatable("wherewasi.timeline.empty"), x + 4, y + 8, UiText.COL_DIM);
            }
        };
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
