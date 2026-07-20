package dev.nokhxyr.wherewasi.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.Zone;
import dev.nokhxyr.wherewasi.record.ActivityRecorder;
import dev.nokhxyr.wherewasi.zones.ZoneTracker;

/**
 * Zone management: name the hot cells WhereWasI detected, and rename / delete /
 * merge existing zones. Selection drives the bottom controls; the name field
 * applies to either a selected zone (rename) or a selected candidate (create).
 */
public final class ZonesScreen extends Screen {

    private final ScrollList list = new ScrollList();
    private EditBox nameInput;
    private Button mergeButton;

    private List<Zone> zones = new ArrayList<>();
    private List<ZoneTracker.Candidate> candidates = new ArrayList<>();

    private String selZone;
    private int selCandidate = -1;
    private String mergeSource;

    public ZonesScreen() {
        super(Component.translatable("wherewasi.zones.title"));
    }

    @Override
    protected void init() {
        list.bounds(20, 40, width - 40, height - 108);

        nameInput = new EditBox(font, 20, height - 60, width - 40, 20, Component.translatable("wherewasi.zones.name_hint"));
        nameInput.setMaxLength(48);
        addRenderableWidget(nameInput);

        int by = height - 32;
        int bw = (width - 40 - 4 * 6) / 5;
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.create_here"), b -> createHere())
                .bounds(20, by, bw, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.apply"), b -> apply())
                .bounds(20 + (bw + 6), by, bw, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.delete"), b -> deleteSelected())
                .bounds(20 + (bw + 6) * 2, by, bw, 20).build());
        mergeButton = Button.builder(mergeLabel(), b -> mergeAction()).bounds(20 + (bw + 6) * 3, by, bw, 20).build();
        addRenderableWidget(mergeButton);
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.close"), b -> onClose())
                .bounds(20 + (bw + 6) * 4, by, bw, 20).build());

        rebuild();
    }

    private void rebuild() {
        ActivityRecorder rec = ClientState.recorder();
        zones = new ArrayList<>(rec.zones().zones());
        candidates = rec.zones().candidates();

        List<ScrollList.Row> rows = new ArrayList<>();
        rows.add(header(Component.translatable("wherewasi.zones.named")));
        if (zones.isEmpty()) {
            rows.add(hint(Component.translatable("wherewasi.zones.none")));
        }
        for (Zone z : zones) {
            rows.add(zoneRow(z));
        }
        rows.add(header(Component.translatable("wherewasi.zones.hot")));
        if (candidates.isEmpty()) {
            rows.add(hint(Component.translatable("wherewasi.zones.no_hot")));
        }
        for (int i = 0; i < candidates.size(); i++) {
            rows.add(candidateRow(candidates.get(i), i));
        }
        list.setRows(rows);
        mergeButton.setMessage(mergeLabel());
    }

    private ScrollList.Row header(Component label) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 16;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                g.drawString(font, label, x + 2, y + 4, UiText.COL_ACCENT);
                g.fill(x, y + 14, x + width, y + 15, UiText.COL_PANEL_LIGHT);
            }
        };
    }

    private ScrollList.Row hint(Component label) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 14;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                g.drawString(font, label, x + 6, y + 3, UiText.COL_DIM);
            }
        };
    }

    private ScrollList.Row zoneRow(Zone z) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 18;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                boolean sel = z.id().equals(selZone);
                if (sel || hovered) {
                    g.fill(x, y, x + width, y + 18, sel ? 0x40FFFFFF : 0x18FFFFFF);
                }
                if (z.id().equals(mergeSource)) {
                    g.fill(x, y, x + 2, y + 18, UiText.COL_WARN);
                }
                // Right cluster: [-] <size> [+] and the dwell time.
                Component t = UiText.duration(z.millis());
                Component minus = Component.literal("[-]");
                Component plus = Component.literal("[+]");
                Component size = Component.literal(z.chunksWide() + "x" + z.chunksDeep());
                int durX = x + width - 8 - font.width(t);
                int plusX = durX - 8 - font.width(plus);
                int sizeX = plusX - 4 - font.width(size);
                int minusX = sizeX - 4 - font.width(minus);
                g.drawString(font, t, durX, y + 5, UiText.COL_DIM);
                g.drawString(font, plus, plusX, y + 5, UiText.COL_ACCENT);
                g.drawString(font, size, sizeX, y + 5, UiText.COL_DIM);
                g.drawString(font, minus, minusX, y + 5, UiText.COL_ACCENT);

                Component line = Component.literal(z.name() + "  ")
                        .append(UiText.dimensionName(z.dim()))
                        .append(Component.literal("  " + z.centerX() + ", " + z.centerZ()));
                int clipRight = minusX - 4;
                if (clipRight > x + 6) {
                    g.enableScissor(x + 6, y, clipRight, y + 18);
                    g.drawString(font, line, x + 6, y + 5, UiText.COL_TEXT);
                    g.disableScissor();
                }
            }

            @Override
            public boolean click(double mouseX, double mouseY, int x, int y, int width) {
                Component t = UiText.duration(z.millis());
                Component minus = Component.literal("[-]");
                Component plus = Component.literal("[+]");
                Component size = Component.literal(z.chunksWide() + "x" + z.chunksDeep());
                int durX = x + width - 8 - font.width(t);
                int plusX = durX - 8 - font.width(plus);
                int sizeX = plusX - 4 - font.width(size);
                int minusX = sizeX - 4 - font.width(minus);
                if (mouseX >= plusX - 3 && mouseX <= plusX + font.width(plus) + 3) {
                    resizeZone(z.id(), 1);
                    return true;
                }
                if (mouseX >= minusX - 3 && mouseX <= minusX + font.width(minus) + 3) {
                    resizeZone(z.id(), -1);
                    return true;
                }
                selZone = z.id();
                selCandidate = -1;
                nameInput.setValue(z.name());
                return true;
            }
        };
    }

    private ScrollList.Row candidateRow(ZoneTracker.Candidate c, int index) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 18;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                boolean sel = index == selCandidate;
                if (sel || hovered) {
                    g.fill(x, y, x + width, y + 18, sel ? 0x40FFFFFF : 0x18FFFFFF);
                }
                Component line = UiText.dimensionName(c.dim())
                        .copy().append(Component.literal("  " + c.x() + ", " + c.z()));
                g.drawString(font, line, x + 6, y + 5, UiText.COL_TEXT);
                Component t = UiText.duration(c.millis());
                g.drawString(font, t, x + width - 8 - font.width(t), y + 5, UiText.COL_DIM);
            }

            @Override
            public boolean click(double mouseX, double mouseY, int x, int y, int width) {
                selCandidate = index;
                selZone = null;
                nameInput.setValue("");
                nameInput.setFocused(true);
                setFocused(nameInput);
                return true;
            }
        };
    }

    private void createHere() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        String name = nameInput.getValue().trim();
        if (p == null || name.isEmpty()) {
            return;
        }
        String dim = p.level().dimension().location().toString();
        BlockPos pos = p.blockPosition();
        ActivityRecorder rec = ClientState.recorder();
        Zone z = rec.zones().createZoneAt(dim, pos.getX(), pos.getZ(), name);
        selZone = z.id();
        selCandidate = -1;
        rec.saveZones();
        rebuild();
    }

    private void resizeZone(String id, int delta) {
        ActivityRecorder rec = ClientState.recorder();
        rec.zones().resize(id, delta);
        selZone = id;
        selCandidate = -1;
        rec.saveZones();
        rebuild();
    }

    private void apply() {
        String name = nameInput.getValue().trim();
        ActivityRecorder rec = ClientState.recorder();
        if (selZone != null && !name.isEmpty()) {
            rec.zones().rename(selZone, name);
            rec.saveZones();
            rebuild();
        } else if (selCandidate >= 0 && selCandidate < candidates.size() && !name.isEmpty()) {
            Zone z = rec.zones().nameCandidate(candidates.get(selCandidate), name);
            selZone = z.id();
            selCandidate = -1;
            rec.saveZones();
            rebuild();
        }
    }

    private void deleteSelected() {
        if (selZone != null) {
            ActivityRecorder rec = ClientState.recorder();
            rec.zones().delete(selZone);
            if (selZone.equals(mergeSource)) {
                mergeSource = null;
            }
            selZone = null;
            rec.saveZones();
            rebuild();
        }
    }

    private void mergeAction() {
        if (selZone == null) {
            return;
        }
        if (mergeSource == null) {
            mergeSource = selZone;
        } else {
            ActivityRecorder rec = ClientState.recorder();
            rec.zones().merge(selZone, mergeSource);
            mergeSource = null;
            rec.saveZones();
        }
        rebuild();
    }

    private Component mergeLabel() {
        return Component.translatable(mergeSource == null ? "wherewasi.btn.merge" : "wherewasi.btn.merge_into");
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
