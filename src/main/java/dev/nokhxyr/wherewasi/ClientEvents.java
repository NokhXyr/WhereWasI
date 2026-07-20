package dev.nokhxyr.wherewasi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import dev.nokhxyr.wherewasi.ui.GuideTarget;
import dev.nokhxyr.wherewasi.ui.HudOverlay;
import dev.nokhxyr.wherewasi.ui.Toasts;

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
        tickGuide();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientState.recorder().onLoggingOut();
        ClientState.clearGuide();
    }

    /** Hides the guide arrow (e.g. a death point) once the player reaches the target. */
    private static void tickGuide() {
        GuideTarget target = ClientState.guideTarget();
        if (target == null || !WhereWasIConfig.CONFIG.guideAutoClear.get()) {
            return;
        }
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null || !p.level().dimension().location().toString().equals(target.dim())) {
            return;
        }
        double dx = target.x() + 0.5 - p.getX();
        double dz = target.z() + 0.5 - p.getZ();
        double r = WhereWasIConfig.CONFIG.guideArrivalRadius.get();
        if (dx * dx + dz * dz <= r * r) {
            ClientState.clearGuide();
            String label = target.label() == null ? "" : target.label();
            Toasts.show(Component.translatable("wherewasi.toast.arrived.title"),
                    Component.translatable("wherewasi.toast.arrived.msg", label));
        }
    }
}
