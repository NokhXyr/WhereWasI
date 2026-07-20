package dev.nokhxyr.wherewasi.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.Note;

/**
 * Notes manager: add a note (with an optional pin) at the top, and below it a
 * scrollable list of existing notes — each with a Pin/Unpin toggle and Delete.
 * Only one note is pinned at a time; pinning one unpins the rest. Enter adds.
 */
public final class NoteScreen extends Screen {

    private final ScrollList list = new ScrollList();
    private EditBox input;
    private Button pinToggle;
    private boolean newPinned;

    public NoteScreen() {
        super(Component.translatable("wherewasi.note.title"));
    }

    @Override
    protected void init() {
        int margin = 20;
        int w = width - margin * 2;
        int addW = 78;
        int pinW = 96;
        int gap = 4;
        int inputW = w - addW - pinW - gap * 2;

        input = new EditBox(font, margin, 44, inputW, 20, Component.translatable("wherewasi.note.hint"));
        input.setMaxLength(256);
        addRenderableWidget(input);
        setInitialFocus(input);

        pinToggle = Button.builder(newPinLabel(), b -> {
            newPinned = !newPinned;
            pinToggle.setMessage(newPinLabel());
        }).bounds(margin + inputW + gap, 44, pinW, 20).build();
        addRenderableWidget(pinToggle);

        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.add"), b -> add())
                .bounds(margin + inputW + gap + pinW + gap, 44, addW, 20).build());

        list.bounds(margin, 72, w, height - 72 - 40);

        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.close"), b -> onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20).build());

        rebuild();
    }

    private Component newPinLabel() {
        return Component.translatable(newPinned ? "wherewasi.note.pin_on" : "wherewasi.note.pin_off");
    }

    private void add() {
        String text = input.getValue().trim();
        if (!text.isEmpty()) {
            ClientState.recorder().createNote(text, newPinned);
            input.setValue("");
            newPinned = false;
            pinToggle.setMessage(newPinLabel());
            rebuild();
        }
    }

    private void rebuild() {
        List<Note> notes = new ArrayList<>(ClientState.recorder().notes());
        notes.sort(Comparator.comparingLong(Note::time).reversed());
        List<ScrollList.Row> rows = new ArrayList<>();
        if (notes.isEmpty()) {
            rows.add(emptyRow());
        }
        for (Note n : notes) {
            rows.add(noteRow(n));
        }
        list.setRows(rows);
    }

    private ScrollList.Row noteRow(Note n) {
        return new ScrollList.Row() {
            @Override
            public int height() {
                return 22;
            }

            @Override
            public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
                if (hovered) {
                    g.fill(x, y, x + width, y + 22, 0x18FFFFFF);
                }
                int textLeft = x + 6;
                if (n.pinned()) {
                    g.drawString(font, "★", x + 4, y + 7, UiText.COL_ACCENT);
                    textLeft = x + 16;
                }
                Component delLbl = Component.translatable("wherewasi.btn.delete");
                Component pinLbl = Component.translatable(n.pinned() ? "wherewasi.btn.unpin" : "wherewasi.btn.pin");
                int delX = x + width - 8 - font.width(delLbl);
                int pinX = delX - 12 - font.width(pinLbl);
                g.drawString(font, delLbl, delX, y + 7, UiText.COL_WARN);
                g.drawString(font, pinLbl, pinX, y + 7, UiText.COL_ACCENT);

                // Clip the note text so it never overlaps the action labels.
                int clipRight = pinX - 6;
                if (clipRight > textLeft) {
                    g.enableScissor(textLeft, y, clipRight, y + 22);
                    g.drawString(font, n.text(), textLeft, y + 7, n.pinned() ? UiText.COL_TITLE : UiText.COL_TEXT);
                    g.disableScissor();
                }
            }

            @Override
            public boolean click(double mouseX, double mouseY, int x, int y, int width) {
                Component delLbl = Component.translatable("wherewasi.btn.delete");
                Component pinLbl = Component.translatable(n.pinned() ? "wherewasi.btn.unpin" : "wherewasi.btn.pin");
                int delX = x + width - 8 - font.width(delLbl);
                int pinX = delX - 12 - font.width(pinLbl);
                if (mouseX >= delX - 3 && mouseX <= delX + font.width(delLbl) + 3) {
                    ClientState.recorder().deleteNote(n.id());
                    rebuild();
                    return true;
                }
                if (mouseX >= pinX - 3 && mouseX <= pinX + font.width(pinLbl) + 3) {
                    if (n.pinned()) {
                        ClientState.recorder().unpinAll();
                    } else {
                        ClientState.recorder().setPinned(n.id());
                    }
                    rebuild();
                    return true;
                }
                return false;
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
                g.drawString(font, Component.translatable("wherewasi.note.empty"), x + 4, y + 8, UiText.COL_DIM);
            }
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && input != null && input.isFocused()) {
            add();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawString(font, title, 20, 22, UiText.COL_TITLE);
        list.render(g, mouseX, mouseY);
    }
}
