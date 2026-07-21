package dev.nokhxyr.wherewasi.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.Session;
import dev.nokhxyr.wherewasi.model.Zone;

/**
 * The logout situation report: shown when the player chooses "Save and Quit" /
 * "Disconnect", it lets them jot a note about where they're at and what's next
 * before actually leaving. Saving pins the note so it heads the next resume
 * briefing; the vanilla quit button is fired only once the player leaves here.
 */
public final class SituationLogScreen extends Screen {

    private final Session snap;
    private final Button quitButton;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int editorTop;

    private MultiLineEditBox editor;

    /**
     * @param snap       a snapshot of the session about to end (for context)
     * @param quitButton the vanilla pause-menu quit button whose action really leaves the world
     */
    public SituationLogScreen(Session snap, Button quitButton) {
        super(Component.translatable("wherewasi.debrief.title"));
        this.snap = snap;
        this.quitButton = quitButton;
    }

    @Override
    protected void init() {
        panelW = Math.min(380, width - 40);
        panelX = (width - panelW) / 2;
        panelY = 30;
        panelH = height - 60;

        int x = panelX + 10;
        editorTop = panelY + 60;
        int buttonsY = panelY + panelH - 28;
        int editorH = Math.max(40, buttonsY - editorTop - 8);

        editor = new MultiLineEditBox(font, x, editorTop, panelW - 20, editorH,
                Component.translatable("wherewasi.log.hint"), Component.translatable("wherewasi.debrief.title"));
        editor.setCharacterLimit(1000);
        addRenderableWidget(editor);
        setInitialFocus(editor);

        int gap = 6;
        int cancelW = 76;
        int leaveW = 90;
        int saveW = (panelW - 20) - cancelW - leaveW - gap * 2;
        int bx = x;
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.save_leave"), b -> saveAndLeave())
                .bounds(bx, buttonsY, saveW, 20).build());
        bx += saveW + gap;
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.leave"), b -> leave())
                .bounds(bx, buttonsY, leaveW, 20).build());
        bx += leaveW + gap;
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.cancel"), b -> onClose())
                .bounds(bx, buttonsY, cancelW, 20).build());
    }

    private void saveAndLeave() {
        String text = editor.getValue().trim();
        if (!text.isEmpty()) {
            ClientState.recorder().createNote(text, true); // pin so the next briefing surfaces it
        }
        leave();
    }

    private void leave() {
        quitButton.onPress();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, UiText.COL_PANEL);
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, UiText.COL_ACCENT);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int x = panelX + 10;

        g.drawString(font, title, x, panelY + 8, UiText.COL_TITLE);
        g.drawString(font, Component.translatable("wherewasi.log.intro"), x, panelY + 22, UiText.COL_DIM);

        MutableComponent session = Component.translatable("wherewasi.log.session", UiText.duration(snap.durationMs()));
        Component did = UiText.sessionHeadline(snap);
        if (!did.getString().isEmpty()) {
            session.append(" · ").append(did);
        }
        g.enableScissor(x, panelY + 33, panelX + panelW - 10, panelY + 43);
        g.drawString(font, session, x, panelY + 34, UiText.COL_TEXT);
        g.disableScissor();

        String zoneName = zoneName(snap.mainZoneId());
        MutableComponent where = Component.translatable("wherewasi.log.pos",
                snap.lastX(), snap.lastY(), snap.lastZ(), UiText.dimensionName(snap.lastDim()));
        if (zoneName != null) {
            where.append(" · ").append(zoneName);
        }
        g.drawString(font, where, x, panelY + 46, UiText.COL_DIM);
    }

    private String zoneName(String zoneId) {
        if (zoneId == null) {
            return null;
        }
        Zone z = ClientState.recorder().zones().zoneById(zoneId);
        return z == null ? null : z.name();
    }

    @Override
    public boolean isPauseScreen() {
        return true; // keep singleplayer paused while writing, like the pause menu it replaced
    }
}
