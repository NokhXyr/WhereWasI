package dev.nokhxyr.wherewasi;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.client.event.ScreenEvent;

import dev.nokhxyr.wherewasi.model.Session;
import dev.nokhxyr.wherewasi.ui.SituationLogScreen;

/**
 * Intercepts the pause menu's "Save and Quit to Title" / "Disconnect" button so a
 * session is never left without a chance to write a situation report. The vanilla
 * button is swapped for one that opens {@link SituationLogScreen}; its own action
 * (the real disconnect) is fired only once the player leaves that screen.
 */
public final class MenuIntercept {

    private MenuIntercept() {
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) {
            return;
        }
        if (!WhereWasIConfig.CONFIG.debriefOnLogout.get() || !ClientState.recorder().sessionActive()) {
            return;
        }
        for (GuiEventListener child : List.copyOf(event.getScreen().children())) {
            if (child instanceof Button btn && isQuitButton(btn.getMessage())) {
                Button replacement = Button.builder(btn.getMessage(), b -> openLog(btn))
                        .bounds(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight())
                        .build();
                event.removeListener(btn);
                event.addListener(replacement);
                return;
            }
        }
    }

    private static void openLog(Button original) {
        Session snap = ClientState.recorder().liveSnapshot();
        if (snap == null) {
            original.onPress(); // no active session to report on — just leave
            return;
        }
        Minecraft.getInstance().setScreen(new SituationLogScreen(snap, original));
    }

    private static boolean isQuitButton(Component msg) {
        if (msg.getContents() instanceof TranslatableContents tc) {
            String key = tc.getKey();
            return "menu.returnToMenu".equals(key) || "menu.disconnect".equals(key);
        }
        return false;
    }
}
