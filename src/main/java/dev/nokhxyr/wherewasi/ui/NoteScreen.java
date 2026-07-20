package dev.nokhxyr.wherewasi.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import dev.nokhxyr.wherewasi.ClientState;

/**
 * Quick note entry: a text field, a pin toggle, save/cancel. The note is bound to
 * the current position/dimension/zone by the recorder. Enter saves.
 */
public final class NoteScreen extends Screen {

    private EditBox input;
    private Button pinButton;
    private boolean pinned;

    public NoteScreen() {
        super(Component.translatable("wherewasi.note.title"));
    }

    @Override
    protected void init() {
        int w = Math.min(300, width - 40);
        int x = (width - w) / 2;
        int y = height / 2 - 34;

        input = new EditBox(font, x, y, w, 20, Component.translatable("wherewasi.note.hint"));
        input.setMaxLength(256);
        addRenderableWidget(input);
        setInitialFocus(input);

        pinButton = Button.builder(pinLabel(), b -> {
            pinned = !pinned;
            pinButton.setMessage(pinLabel());
        }).bounds(x, y + 26, w, 20).build();
        addRenderableWidget(pinButton);

        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.save"), b -> save())
                .bounds(x, y + 52, w / 2 - 2, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.cancel"), b -> onClose())
                .bounds(x + w / 2 + 2, y + 52, w / 2 - 2, 20).build());
    }

    private Component pinLabel() {
        return Component.translatable(pinned ? "wherewasi.note.pin_on" : "wherewasi.note.pin_off");
    }

    private void save() {
        String text = input.getValue().trim();
        if (!text.isEmpty()) {
            ClientState.recorder().createNote(text, pinned);
        }
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawString(font, title, (width - font.width(title)) / 2, height / 2 - 54, UiText.COL_TITLE);
    }
}
