package dev.nokhxyr.wherewasi;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import dev.nokhxyr.wherewasi.ui.HudOverlay;

/**
 * Game- and mod-bus event handlers. Everything here is registered from the
 * client-only {@code @Mod} constructor, so none of it exists on a dedicated server.
 */
public final class ClientEvents {

    private ClientEvents() {
    }

    // ---- mod bus -----------------------------------------------------------

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(WhereWasI.MOD_ID, "hud"), new HudOverlay());
    }

    // ---- game bus ----------------------------------------------------------

    public static void onClientTick(ClientTickEvent.Post event) {
        ClientState.recorder().tick();
        Keybinds.handle();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientState.recorder().onLoggingOut();
        ClientState.clearGuide();
    }
}
