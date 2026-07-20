package dev.nokhxyr.wherewasi;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import dev.nokhxyr.wherewasi.ui.NoteScreen;
import dev.nokhxyr.wherewasi.ui.TimelineScreen;
import dev.nokhxyr.wherewasi.ui.Toasts;
import dev.nokhxyr.wherewasi.ui.ZoneClaimScreen;
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
    public static final KeyMapping MARK_ZONE = new KeyMapping(
            "key.wherewasi.mark_zone", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);

    private Keybinds() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_JOURNAL);
        event.register(OPEN_NOTE);
        event.register(OPEN_BRIEFING);
        event.register(OPEN_ZONES);
        event.register(CLEAR_GUIDE);
        event.register(MARK_ZONE);
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
        if (MARK_ZONE.consumeClick()) {
            markZoneCorner(mc);
        }
    }

    /**
     * Claim flow: first press stores the corner at the player's position; the second
     * press (in the same dimension) opens the naming screen for the claimed box.
     */
    private static void markZoneCorner(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) {
            return;
        }
        String dim = p.level().dimension().location().toString();
        BlockPos pos = p.blockPosition();
        ClientState.Corner first = ClientState.pendingCorner();
        if (first == null || !first.dim().equals(dim)) {
            ClientState.setPendingCorner(new ClientState.Corner(dim, pos.getX(), pos.getZ()));
            Toasts.show(Component.translatable("wherewasi.toast.claim1.title"),
                    Component.translatable("wherewasi.toast.claim1.msg"));
        } else {
            ClientState.clearPendingCorner();
            mc.setScreen(new ZoneClaimScreen(dim, first.x(), first.z(), pos.getX(), pos.getZ()));
        }
    }
}
