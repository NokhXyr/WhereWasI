package dev.nokhxyr.wherewasi;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import dev.nokhxyr.wherewasi.ui.NoteScreen;
import dev.nokhxyr.wherewasi.ui.TimelineScreen;
import dev.nokhxyr.wherewasi.ui.ZonesScreen;

/**
 * Keybindings (registered on the mod bus, client only) and the per-tick dispatch
 * that opens the matching screen. Category string is a plain translation key —
 * 1.21.1 has no {@code KeyMapping.Category} type.
 */
public final class Keybinds {

    private static final String CATEGORY = "key.categories.wherewasi";

    public static final KeyMapping OPEN_JOURNAL = new KeyMapping(
            "key.wherewasi.journal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    public static final KeyMapping OPEN_NOTE = new KeyMapping(
            "key.wherewasi.note", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);
    public static final KeyMapping OPEN_BRIEFING = new KeyMapping(
            "key.wherewasi.briefing", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping OPEN_ZONES = new KeyMapping(
            "key.wherewasi.zones", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping CLEAR_GUIDE = new KeyMapping(
            "key.wherewasi.clear_guide", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

    private Keybinds() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_JOURNAL);
        event.register(OPEN_NOTE);
        event.register(OPEN_BRIEFING);
        event.register(OPEN_ZONES);
        event.register(CLEAR_GUIDE);
    }

    /** Called each client tick; opens a screen when its key was pressed in-game. */
    public static void handle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (OPEN_JOURNAL.consumeClick()) {
            mc.setScreen(new TimelineScreen());
        }
        if (OPEN_NOTE.consumeClick()) {
            mc.setScreen(new NoteScreen());
        }
        if (OPEN_BRIEFING.consumeClick()) {
            ClientState.recorder().openBriefing(true);
        }
        if (OPEN_ZONES.consumeClick()) {
            mc.setScreen(new ZonesScreen());
        }
        if (CLEAR_GUIDE.consumeClick()) {
            ClientState.clearGuide();
        }
    }
}
