package dev.nokhxyr.wherewasi.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import dev.nokhxyr.wherewasi.ClientState;
import dev.nokhxyr.wherewasi.model.Zone;

/**
 * Names the area claimed by two corners. Opened by the "mark zone corner" keybind
 * once the second corner is set; shows the chunk-aligned box that will be claimed.
 */
public final class ZoneClaimScreen extends Screen {

    private final String dim;
    private final int x1;
    private final int z1;
    private final int x2;
    private final int z2;
    private final int[] box;
    private EditBox input;

    public ZoneClaimScreen(String dim, int x1, int z1, int x2, int z2) {
        super(Component.translatable("wherewasi.claim.title"));
        this.dim = dim;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.box = Zone.boxFromPoints(x1, z1, x2, z2);
    }

    @Override
    protected void init() {
        int w = Math.min(300, width - 40);
        int x = (width - w) / 2;
        int y = height / 2 - 8;

        input = new EditBox(font, x, y, w, 20, Component.translatable("wherewasi.zones.name_hint"));
        input.setMaxLength(48);
        addRenderableWidget(input);
        setInitialFocus(input);

        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.claim"), b -> create())
                .bounds(x, y + 28, w / 2 - 2, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("wherewasi.btn.cancel"), b -> onClose())
                .bounds(x + w / 2 + 2, y + 28, w / 2 - 2, 20).build());
    }

    private void create() {
        String name = input.getValue().trim();
        if (!name.isEmpty()) {
            Zone z = ClientState.recorder().zones().createClaim(dim, x1, z1, x2, z2, name);
            ClientState.recorder().saveZones();
            Toasts.show(Component.translatable("wherewasi.toast.claim2.title"),
                    Component.translatable("wherewasi.toast.claim2.msg", z.name(), z.chunksWide() * z.chunksDeep()));
        }
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && input != null && input.isFocused()) {
            create();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx = width / 2;
        g.drawString(font, title, cx - font.width(title) / 2, height / 2 - 54, UiText.COL_TITLE);

        int chunksW = (box[2] - box[0] + 1) / 16;
        int chunksD = (box[3] - box[1] + 1) / 16;
        Component info = Component.translatable("wherewasi.claim.info", chunksW, chunksD, chunksW * chunksD);
        g.drawString(font, info, cx - font.width(info) / 2, height / 2 - 34, UiText.COL_TEXT);
        Component coords = Component.literal(box[0] + ", " + box[1] + "  ->  " + box[2] + ", " + box[3]);
        g.drawString(font, coords, cx - font.width(coords) / 2, height / 2 - 22, UiText.COL_DIM);
    }
}
